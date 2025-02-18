package com.AirBndProject.advices;

import com.AirBndProject.exceptions.ResourceNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler
{

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(ApiError apiError)
    {
        return new ResponseEntity<>(new ApiResponse<>(apiError),apiError.getStatus());
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException exc)
    {
       ApiError apiError = ApiError.builder()
               .message(exc.getMessage())
               .status(HttpStatus.NOT_FOUND)
               .build();

       return buildErrorResponse(apiError);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleInternalServerError(Exception exc)
    {
        ApiError ae= ApiError.builder()
                .message("Sorry !!, Server not responding -> " + exc.getLocalizedMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return buildErrorResponse(ae);
    }




    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> InvalidDataInput(MethodArgumentNotValidException manve)
    {
        List<String> err=manve.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ApiError ae= ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Input Arguments is not Valid")
                .subErrors(err)
                .build();

        return buildErrorResponse(ae);

    }
}
