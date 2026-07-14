package com.meteobridge.weather.model;

import java.util.List;

public record ForecastWeather(
    String city,
    String country,
    List<ForecastItem> forecast
) {
    public record ForecastItem(
        String dateTime,
        double temperature,
        double feelsLike,
        int humidity,
        String description,
        double windSpeed
    ) {}
}
