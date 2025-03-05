package com.AirBndProject.service;

import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.dto.HotelInfoDto;
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

import static com.AirBndProject.util.AppUtils.getCurrentUser;

@Service
@Slf4j // for logging purposes
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService
{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;


    @Override
    public HotelDto createNewHotel(HotelDto hotelDto)
    {
        log.info("Creating a new hotel with name: {}",hotelDto.getName());
        Hotel hotel=modelMapper.map(hotelDto,Hotel.class);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel.setActive(false);
        return modelMapper.map(hotelRepository.save(hotel),HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id)
    {
        log.info("Fetching hotel with id : {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ id));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This user don not own this hotel with id ->"+id);
        }

        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with id : {}",id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This user don not own this hotel with id ->"+id);
        }

        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        return modelMapper.map(hotelRepository.save(hotel),HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id)
    {
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ id));
        log.info("found hotel with id -> "+ id+ " --- "+hotel);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This user don not own this hotel with id ->"+id);
        }

        for(Room room:hotel.getRooms())
        {
            log.info("traverse hotel rooms - id = -> "+ room.getId());
            inventoryService.deleteAllinventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);



    }

    @Override
    @Transactional
    public void activateHotel(Long id)
    {
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This user don not own this hotel with id ->"+id);
        }
        if(!hotel.getActive())
        {
            hotel.setActive(true);

            for (Room room : hotel.getRooms()) {
                inventoryService.initializeRoomForOneYear(room);
            }
        }

        hotelRepository.save(hotel);
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel=hotelRepository
                .findById(hotelId)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ hotelId));
        List<RoomDto> roomDtos = hotel.getRooms().stream().map((element) -> modelMapper.map(element, RoomDto.class)).toList();


        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),roomDtos);
    }

    @Override
    public List<HotelDto> getAllHotels()
    {
        User user = getCurrentUser();
        log.info("Getting all hotels for the admin user with ID: {}", user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);

        return hotels
                .stream()
                .map((element) -> modelMapper.map(element, HotelDto.class))
                .collect(Collectors.toList());
    }


}
