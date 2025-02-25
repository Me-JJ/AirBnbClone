package com.AirBndProject.service;

import com.AirBndProject.dto.RoomDto;
import com.AirBndProject.entities.Hotel;
import com.AirBndProject.entities.Room;
import com.AirBndProject.entities.User;
import com.AirBndProject.exceptions.ResourceNotFoundException;
import com.AirBndProject.exceptions.UnAuthorizedException;
import com.AirBndProject.repository.HotelRepository;
import com.AirBndProject.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public RoomDto createNewRoom(Long hotelId, RoomDto rootDto)
    {
        log.info("Fetching Hotel with ID -> {}",hotelId);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException(("Hotel not found with Id -> " + hotelId)));

        log.info("Hotel found -> {}",hotel.getId());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This user don not own this hotel with id ->"+hotelId);
        }

        Room room = modelMapper.map(rootDto,Room.class);
        room.setHotel(hotel);

        Room room1 = roomRepository.save(room);

        log.info("Set room with hotel -> {}",room1.toString());

        if (hotel.getActive())
        {
            log.info("Initializing Room after hotel is found active -> {}",room);
            inventoryService.initializeRoomForOneYear(room);
        }

        return modelMapper.map(room1,RoomDto.class);

    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId)
    {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id -> " + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This user don not own this hotel with id ->"+hotelId);
        }

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
    @Transactional
    public void deleteRoomById(Long roomId)
    {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with Id -> " + roomId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner()))
        {
            throw new UnAuthorizedException("This user don not own this room with id ->"+room.getHotel().getCity());
        }

        inventoryService.deleteAllinventories(room);
        roomRepository.deleteById(roomId);

    }
}
