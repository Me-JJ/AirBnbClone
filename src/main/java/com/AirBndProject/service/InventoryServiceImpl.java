package com.AirBndProject.service;

import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.dto.HotelPriceDto;
import com.AirBndProject.dto.HotelSearchRequest;
import com.AirBndProject.entities.Hotel;
import com.AirBndProject.entities.Inventory;
import com.AirBndProject.entities.Room;
import com.AirBndProject.repository.HotelMinPriceRepository;
import com.AirBndProject.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService
{
    private final ModelMapper modelMapper;

    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;

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
}
