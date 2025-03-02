package com.AirBndProject.dto;

import com.AirBndProject.entities.Hotel;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HotelPriceDto
{
    private Hotel hotel;
    private Double price;
}
