package com.AirBndProject.service;


import com.AirBndProject.dto.ProfileUpdateRequestDto;
import com.AirBndProject.dto.UserDto;
import com.AirBndProject.entities.User;

public interface UserService
{
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
