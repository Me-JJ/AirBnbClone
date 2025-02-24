package com.AirBndProject.repository;

import com.AirBndProject.dto.HotelPriceDto;
import com.AirBndProject.entities.Hotel;
import com.AirBndProject.entities.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice,Long>
{
    //com.AirBndProject.dto -> we can also specify dto pkg to get the query result

@Query("""
            SELECT new com.AirBndProject.dto.HotelPriceDto(i.hotel, AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND i.hotel.active = true
           GROUP BY i.hotel
          """)
Page<HotelPriceDto> findHotelWithAvailableInventory(
        @Param("city") String city,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("roomsCount") Integer roomsCount,
        @Param("dateCount") Long dateCount,
        Pageable pageable
);

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
