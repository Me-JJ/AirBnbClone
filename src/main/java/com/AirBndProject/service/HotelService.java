package com.AirBndProject.service;

import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.dto.HotelInfoDto;

import java.util.List;

public interface HotelService
{
    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id,HotelDto hotelDto);

    void deleteHotelById(Long id);
    void activateHotel(Long id);

    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotels();
}
