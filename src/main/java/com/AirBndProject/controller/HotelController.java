package com.AirBndProject.controller;

import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.service.HotelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/admin/hotels")
@Slf4j
public class HotelController
{
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

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
}
