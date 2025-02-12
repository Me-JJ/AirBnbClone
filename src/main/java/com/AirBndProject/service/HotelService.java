package com.AirBndProject.service;

import com.AirBndProject.dto.HotelDto;

public interface HotelService
{
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    void deleteHotelById(Long id);
    void activateHotel(Long id);
}
