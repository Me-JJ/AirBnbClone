package com.AirBndProject.service;

import com.AirBndProject.dto.*;
import com.AirBndProject.entities.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    void initializeRoomForOneYear(Room room);

    void deleteAllinventories(Room room);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
