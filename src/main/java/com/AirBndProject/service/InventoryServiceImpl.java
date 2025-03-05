package com.AirBndProject.service;

import com.AirBndProject.dto.*;
import com.AirBndProject.entities.Hotel;
import com.AirBndProject.entities.Inventory;
import com.AirBndProject.entities.Room;
import com.AirBndProject.entities.User;
import com.AirBndProject.exceptions.ResourceNotFoundException;
import com.AirBndProject.repository.HotelMinPriceRepository;
import com.AirBndProject.repository.InventoryRepository;
import com.AirBndProject.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.AirBndProject.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService
{
    private final ModelMapper modelMapper;

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final RoomRepository roomRepository;

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest)
    {
        log.info("Searching hotels for {} city, from {} to {}", hotelSearchRequest.getCity(), hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate());
        log.info("HotelSearch -> "+hotelSearchRequest.toString());
//         we need to get all the hotels in particular city that have at-least
//         one room type available in the mentioned time period !
//         criteria for filtering inventory :
//                     -> startData <= date <= endDate
//                     -> city
//                     availability : totalCount - bookedCount >= roomsCount
//            group the response by room & get the response by unique hotels
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getSize());
        long dateCount =
                ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;

        // business logic - 90 days

//        return hotelPage.map((element) -> modelMapper.map(element, HotelDto.class));
        return hotelMinPriceRepository.findHotelWithAvailableInventory(hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate()
        ,hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),dateCount,pageable);
    }

    @Override
    public void initializeRoomForOneYear(Room room)
    {
        log.info("initializing room for 1 year");
        LocalDate today = LocalDate.now();

        LocalDate endDate= today.plusYears(1);

        while(!today.isAfter(endDate))
        {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .reservedCount(0)
                    .closed(false)
                    .build();

            inventoryRepository.save(inventory);
            today=today.plusDays(1);
        }

    }

    @Override
    public void deleteAllinventories(Room room)
    {
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting All inventory by room for room with id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+roomId));

        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map((element) -> modelMapper.map(element,
                        InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating All inventory by room for room with id: {} between date range: {} - {}", roomId,
                updateInventoryRequestDto.getStartDate(), updateInventoryRequestDto.getEndDate());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+roomId));

        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of room with id: "+roomId);

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(), updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor());
    }


}
