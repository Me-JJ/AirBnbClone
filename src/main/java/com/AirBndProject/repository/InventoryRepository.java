package com.AirBndProject.repository;

import com.AirBndProject.entities.Inventory;
import com.AirBndProject.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    void deleteByDateAfterAndRoom(LocalDate today, Room room);
}
