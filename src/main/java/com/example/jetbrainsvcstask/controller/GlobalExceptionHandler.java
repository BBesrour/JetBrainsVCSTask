package com.example.jetbrainsvcstask.controller;

import com.example.jetbrainsvcstask.NotFoundException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IOException.class, RuntimeException.class,})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIOException(IOException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "form";
    }
    @ExceptionHandler({MalformedURLException.class, URISyntaxException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleUrlExceptions(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "form";
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "form";
    }

}