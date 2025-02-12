package com.AirBndProject.dto;

import com.AirBndProject.entities.HotelContactInfo;
import lombok.*;

@Data
@Getter
@Setter
public class HotelDto
{
    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private Boolean active;
    private HotelContactInfo contactInfo;

}
