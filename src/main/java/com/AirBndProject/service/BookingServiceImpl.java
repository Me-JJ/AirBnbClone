package com.AirBndProject.service;

import com.AirBndProject.dto.BookingDto;
import com.AirBndProject.dto.BookingRequest;
import com.AirBndProject.dto.GuestDto;
import com.AirBndProject.entities.*;
import com.AirBndProject.entities.enums.BookingStatus;
import com.AirBndProject.exceptions.ResourceNotFoundException;
import com.AirBndProject.exceptions.UnAuthorizedException;
import com.AirBndProject.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest)
    {
        log.info("request body -> {}", bookingRequest.toString());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id -> " + bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with Id -> " + bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository
                .findAndLockAvailableInventory(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(),bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        if(inventoryList.size() != daysCount)
        {
            throw new IllegalStateException("Room is not available anymore");
        }

        // reserve the room/ update the booked count of inventories

        for(Inventory inventory:inventoryList)
        {
            inventory.setReservedCount(inventory.getReservedCount() + bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventoryList);

        //create the booking

//        TODO:calculate dynamic pricing

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();

        log.info("Booking ->"+booking.getId() + "\t"+booking.getRoomsCount());
        return modelMapper.map( bookingRepository.save(booking),BookingDto.class);
    }

    @Override
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

    public boolean hasBookingExpired(Booking booking)
    {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }




    public User getCurrentUser()
    {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
