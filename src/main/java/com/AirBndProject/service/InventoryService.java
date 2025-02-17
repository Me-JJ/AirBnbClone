package com.AirBndProject.service;

import com.AirBndProject.entities.Room;

public interface InventoryService {
    void initializeRoomForOneYear(Room room);

    void deleteAllinventories(Room room);

}
