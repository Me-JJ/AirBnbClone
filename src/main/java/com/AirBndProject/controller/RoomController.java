package com.AirBndProject.controller;

import com.AirBndProject.dto.RoomDto;
import com.AirBndProject.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController
{

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomDto> createNewRoom( @PathVariable Long hotelId,@RequestBody RoomDto roomDto)
    {
        log.info("creating new room in hotel with id {}",hotelId);
        return new ResponseEntity<>(roomService.createNewRoom(hotelId,roomDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDto>> getAllRoomsInHotel(@PathVariable Long hotelId)
    {
        return ResponseEntity.ok(roomService.getAllRoomsInHotel(hotelId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long roomId)
    {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoomById(@PathVariable Long hotelId,@PathVariable Long roomId)
    {
        roomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();

    }
}
