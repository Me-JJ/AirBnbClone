package com.AirBndProject.service;

import com.AirBndProject.dto.BookingDto;
import com.AirBndProject.dto.BookingRequest;
import com.AirBndProject.dto.GuestDto;
import com.AirBndProject.dto.HotelReportDto;
import com.AirBndProject.entities.*;
import com.AirBndProject.entities.enums.BookingStatus;
import com.AirBndProject.exceptions.ResourceNotFoundException;
import com.AirBndProject.exceptions.UnAuthorizedException;
import com.AirBndProject.repository.*;
import com.AirBndProject.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.AirBndProject.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService
{
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final PricingService pricingService;
    private final CheckoutService checkoutService;

    @Value("${frontend.url}")
    private String frontEndUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest)
    {
        log.info("request body -> {}", bookingRequest.toString());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id -> " + bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with Id -> " + bookingRequest.getRoomId()));

        if(!hotel.getRooms().contains(room))
        {
            throw new ResourceNotFoundException("Room not found with Id -> " + bookingRequest.getRoomId() + " in hotel with ID: "+hotel.getId());
        }
        log.info("room-> {}",room.getId());
        List<Inventory> inventoryList = inventoryRepository
                .findAndLockAvailableInventory(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        if(inventoryList.size() != daysCount)
        {
            throw new IllegalStateException("Room is not available anymore");
        }

        // reserve the room/ update the booked count of inventories

//before
//        for(Inventory inventory:inventoryList)
//        {
//            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
//        }
//        inventoryRepository.saveAll(inventoryList);


        //new implementation for the above code
        inventoryRepository.initBooking(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());


//      calculate dynamic pricing

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        //create the booking

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        log.info("Booking ->"+booking.getId() + "\t"+booking.getRoomsCount());
        return modelMapper.map( bookingRepository.save(booking),BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList)
    {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("Booking not found with ID -> "+bookingId));

        User user = getCurrentUser();

        if (!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Booking does not belong to this User with id "+user.getId());
        }

        if(hasBookingExpired(booking))
        {
            throw new IllegalStateException("Booking has already expired");
        }
        if (booking.getBookingStatus() != BookingStatus.RESERVED)
        {
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }

        for (GuestDto guestDto:guestDtoList)
        {
            Guest guest = modelMapper.map(guestDto,Guest.class);
            guest.setUser(user);
            booking.getGuests().add(guestRepository.save(guest));
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        return modelMapper.map(bookingRepository.save(booking),BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId)
    {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()-> new ResourceNotFoundException("Booking id not found with id -> "+bookingId));
        User user = getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(hasBookingExpired(booking))
        {
            //if expired then undo the bookingCounts or check if the cron update can handle that
            //payment is pending, but it has expired so we had initialized the payment, but the booked count is still
            //incremented
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,frontEndUrl+"payments/success",frontEndUrl+"payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENTS_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event)
    {
        log.info("Capturing Payment-> {}",event.getType());
        if("checkout.session.completed".equals(event.getType()))
        {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if(session == null)
            {
                return;
            }

            String sessionId= session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId)
                            .orElseThrow(()-> new ResourceNotFoundException("Booking not found with session id: "+ sessionId));

            log.info("Setting booking status to confirm");
            booking.setBookingStatus(BookingStatus.CONFIRMED);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId()
                    ,booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking Id: {}",booking.getId());
        }
        else log.warn("Unhandled event type: {}",event.getType());
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId)
    {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("Booking not found with ID -> "+bookingId));

        User user = getCurrentUser();

        if (!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Booking does not belong to this User with id "+user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED)
        {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId()
                ,booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getRoomsCount());

        // handle the refund
        try
        {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();
            Refund.create(refundCreateParams);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        log.info("Successfully Cancelled the booking for Booking Id: {}",booking.getId());
    }

    @Override
    public String getBookingStatus(Long bookingId)
    {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->
                new ResourceNotFoundException("Booking not found with ID -> "+bookingId));

        User user = getCurrentUser();

        if (!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Booking does not belong to this User with id "+user.getId());
        }

        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelDto(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not " +
                "found with ID: "+hotelId));
        User user = getCurrentUser();

        log.info("Getting all booking for the hotel with ID: {}", hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: "+hotelId);

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        List<BookingDto> bookingDtoList=bookings.stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .toList();

        return bookingDtoList;
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not " +
                "found with ID: "+hotelId));
        User user = getCurrentUser();

        log.info("Generating report for hotel with ID: {}", hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: "+hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                .stream().
                map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    public boolean hasBookingExpired(Booking booking)
    {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }


}
