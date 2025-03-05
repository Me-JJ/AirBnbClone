package com.AirBndProject.controller;

import com.AirBndProject.dto.BookingDto;
import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.dto.HotelReportDto;
import com.AirBndProject.service.BookingService;
import com.AirBndProject.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/hotels")
@Slf4j
@RequiredArgsConstructor
public class HotelController
{
    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> createNewHotel(@RequestBody HotelDto hotelDto)
    {
        log.info("Attempting to create a new hotel with name : {}", hotelDto.getName());
        return new ResponseEntity<>(hotelService.createNewHotel(hotelDto), HttpStatus.CREATED);
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long id)
    {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long id,@RequestBody HotelDto hotelDto)
    {
        HotelDto hotel=hotelService.updateHotelById(id,hotelDto);
        return ResponseEntity.ok(hotel);

    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long id)
    {
        hotelService.deleteHotelById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{id}/activate")
    public ResponseEntity<Void> activeHotel(@PathVariable Long id)
    {
        hotelService.activateHotel(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping
    public ResponseEntity<List<HotelDto>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingsByHotelDto(@PathVariable Long hotelId)
    {
        List<BookingDto> bookingDtoList =bookingService.getAllBookingsByHotelDto(hotelId);
        return ResponseEntity.ok(bookingDtoList);
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required = false) LocalDate startDate,
                                                         @RequestParam(required = false) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        return ResponseEntity.ok(bookingService.getHotelReport(hotelId, startDate, endDate));
    }

}
