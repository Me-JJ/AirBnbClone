package com.AirBndProject.service;

import com.AirBndProject.entities.Room;

import java.time.LocalDate;

public interface InventoryService {
    void initializeRoomForOneYear(Room room);

    void deleteFutureInventories(Room room);

}
