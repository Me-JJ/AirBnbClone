package com.AirBndProject.dto;

import com.AirBndProject.entities.Hotel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelPriceDto
{
    private Hotel hotel;
    private Double price;
}
