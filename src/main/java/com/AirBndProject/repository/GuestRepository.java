package com.AirBndProject.repository;

import com.AirBndProject.entities.Guest;
import com.AirBndProject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}