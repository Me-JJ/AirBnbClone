package com.AirBndProject.dto;

import com.AirBndProject.entities.Guest;
import com.AirBndProject.entities.Hotel;
import com.AirBndProject.entities.Room;
import com.AirBndProject.entities.User;
import com.AirBndProject.entities.enums.BookingStatus;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@ToString
public class BookingDto
{
    private Long id;
//    private Hotel hotel;
//    private Room room;
//    private User user;
    private BigDecimal amount;
    private Integer roomsCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
}
