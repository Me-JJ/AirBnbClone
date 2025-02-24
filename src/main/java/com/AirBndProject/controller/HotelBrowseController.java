package com.AirBndProject.controller;


import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.dto.HotelInfoDto;
import com.AirBndProject.dto.HotelPriceDto;
import com.AirBndProject.dto.HotelSearchRequest;
import com.AirBndProject.service.HotelService;
import com.AirBndProject.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelBrowseController
{
    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest)
    {
        Page<HotelPriceDto> page = inventoryService.searchHotels(hotelSearchRequest);
        log.info("search results  -> {} ", page);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId)
    {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
