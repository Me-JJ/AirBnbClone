package com.AirBndProject.service;

import com.AirBndProject.dto.HotelDto;
import com.AirBndProject.entities.Hotel;
import com.AirBndProject.exceptions.ResourceNotFoundException;
import com.AirBndProject.repository.HotelRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j // for logging purposes

public class HotelServiceImpl implements HotelService
{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;

    public HotelServiceImpl(HotelRepository hotelRepository, ModelMapper modelMapper)
    {
        this.hotelRepository = hotelRepository;
        this.modelMapper = modelMapper;
    }



    @Override
    public HotelDto createNewHotel(HotelDto hotelDto)
    {
        log.info("Creating a new hotel with name: {}",hotelDto.getName());
        Hotel hotel=modelMapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        log.info("Created hotel with id: {}",hotelDto.getId());
        return modelMapper.map(hotelRepository.save(hotel),HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id)
    {
        log.info("Fetching hotel with id : {}",id);
        return modelMapper.map(hotelRepository
                .findById(id)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ id)),
                HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with id : {}",id);
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ id));

        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        return modelMapper.map(hotelRepository.save(hotel),HotelDto.class);
    }

    public void deleteHotelById(Long id)
    {
        boolean exists = hotelRepository.existsById(id);
        if(!exists) throw new ResourceNotFoundException("Hotel not found with Id -> "+ id);

        hotelRepository.deleteById(id);
//        TODO : delete the future inventories for this hotel
    }

    @Override
    public void activateHotel(Long id)
    {
        Hotel hotel=hotelRepository
                .findById(id)
                .orElseThrow(
                        ()->new ResourceNotFoundException("Hotel not found with Id -> "+ id));

        hotel.setActive(true);
        hotelRepository.save(hotel);
//         TODO : Create inventory for all the rooms for this hotel
    }


}
