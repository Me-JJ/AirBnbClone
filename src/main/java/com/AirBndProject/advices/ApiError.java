package com.AirBndProject.advices;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError
{
    private String message;
    private HttpStatus status;
    private List<String> subErrors;


}
