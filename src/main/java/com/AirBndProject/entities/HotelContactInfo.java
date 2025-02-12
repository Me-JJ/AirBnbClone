package com.AirBndProject.entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Data
public class HotelContactInfo
{
    private String address;

    private String phoneNumber;

    private String location;

    private String email;
}
