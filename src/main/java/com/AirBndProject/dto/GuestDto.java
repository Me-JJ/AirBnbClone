package com.AirBndProject.dto;

import com.AirBndProject.entities.User;
import com.AirBndProject.entities.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto
{
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
