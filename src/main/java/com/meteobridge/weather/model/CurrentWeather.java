package com.meteobridge.weather.model;

public record CurrentWeather(
    String city,
    String country,
    double temperature,
    double feelsLike,
    int humidity,
    double windSpeed,
    String description
) {}
