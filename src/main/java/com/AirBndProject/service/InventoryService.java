package com.AirBndProject.service;

import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.dto.HotelPriceDto;
import com.AirBndProject.dto.HotelSearchRequest;
import com.AirBndProject.entities.Room;
import org.springframework.data.domain.Page;

public interface InventoryService {
    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    void initializeRoomForOneYear(Room room);

    void deleteAllinventories(Room room);

}
