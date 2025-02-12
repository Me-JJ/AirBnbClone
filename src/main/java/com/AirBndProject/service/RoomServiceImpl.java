package com.AirBndProject.service;

import com.AirBndProject.dto.RoomDto;
import com.AirBndProject.entities.Hotel;
import com.AirBndProject.entities.Room;
import com.AirBndProject.exceptions.ResourceNotFoundException;
import com.AirBndProject.repository.HotelRepository;
import com.AirBndProject.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.modelmapper.ModelMapper;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Service;

import java.security.PrivilegedAction;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto rootDto)
    {
        log.debug("Fetching Hotel with ID -> {}",hotelId);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException(("Hotel not found with Id -> " + hotelId)));

        log.debug("Hotel found -> {}",hotel.getId());

        Room room = modelMapper.map(rootDto,Room.class);
        room.setHotel(hotel);

        log.debug("Set room with hotel -> {}",room);

//        TODO : create inventory asa room is created and if hotle is active
        return modelMapper.map(roomRepository.save(room),RoomDto.class);

    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId)
    {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id -> " + hotelId));

        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper
                        .map(element, RoomDto.class))
                        .collect(Collectors.toList());
    }



    @Override
    public RoomDto getRoomById(Long roomId)
    {
        return modelMapper.map(roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with Id -> " + roomId)),
                RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId)
    {
        boolean exists = roomRepository.existsById(roomId);

        if(!exists) throw  new ResourceNotFoundException("Room not found with Id -> " + roomId);

        roomRepository.deleteById(roomId);

//         TODO : Delete All future room inventories for this room
    }
}
