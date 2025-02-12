package com.AirBndProject.service;

import com.AirBndProject.dto.RoomDto;

import java.util.List;

public interface RoomService
{
    RoomDto createNewRoom(Long hotelId,RoomDto rootDto);

    List<RoomDto> getAllRoomsInHotel(Long hotelId);

    RoomDto getRoomById(Long roomId);

    void deleteRoomById(Long roomId);
}
