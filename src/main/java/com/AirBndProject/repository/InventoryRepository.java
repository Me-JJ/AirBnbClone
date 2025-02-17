package com.AirBndProject.repository;

import com.AirBndProject.entities.Inventory;
import com.AirBndProject.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    void deleteByRoom(Room room);
}
