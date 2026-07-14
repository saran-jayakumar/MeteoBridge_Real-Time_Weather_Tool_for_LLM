package com.meteobridge.weather.service;

import com.meteobridge.weather.model.CurrentWeather;
import com.meteobridge.weather.model.ForecastWeather;
import com.meteobridge.weather.model.ForecastWeather.ForecastItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private final RestClient restClient;
    private final String apiKey;

    public WeatherService(
            RestClient.Builder restClientBuilder,
            @Value("${openweather.api.base-url}") String baseUrl,
            @Value("${openweather.api.key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @McpTool(name = "get_current_weather", description = "Retrieves the current weather information for a specific city.")
    public CurrentWeather getCurrentWeather(
            @McpToolParam(description = "The city name (e.g. London, Tokyo)", required = true) String city,
            @McpToolParam(description = "Two-letter ISO country code (e.g. US, GB, JP) to make search precise", required = false) String countryCode,
            @McpToolParam(description = "Temperature units: 'metric' (default, Celsius), 'imperial' (Fahrenheit), or 'standard' (Kelvin)", required = false) String units) {
        
        String unitParam = (units == null || units.isBlank()) ? "metric" : units;
        String query = (countryCode == null || countryCode.isBlank()) ? city : city + "," + countryCode;

        logger.info("Fetching current weather for: {}, units: {}", query, unitParam);

        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("q", query)
                            .queryParam("appid", apiKey)
                            .queryParam("units", unitParam)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new RuntimeException("Empty response from OpenWeather API");
            }

            Map<String, Object> main = (Map<String, Object>) response.get("main");
            Map<String, Object> sys = (Map<String, Object>) response.get("sys");
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
            
            String desc = "Unknown";
            if (weatherList != null && !weatherList.isEmpty()) {
                desc = (String) weatherList.get(0).get("description");
            }

            String country = sys != null ? (String) sys.get("country") : "";
            double temp = main != null ? ((Number) main.get("temp")).doubleValue() : 0.0;
            double feelsLike = main != null ? ((Number) main.get("feels_like")).doubleValue() : 0.0;
            int humidity = main != null ? ((Number) main.get("humidity")).intValue() : 0;
            double windSpeed = wind != null ? ((Number) wind.get("speed")).doubleValue() : 0.0;
            String name = (String) response.get("name");

            return new CurrentWeather(name, country, temp, feelsLike, humidity, windSpeed, desc);

        } catch (Exception e) {
            logger.error("Failed to fetch current weather for {}: {}", query, e.getMessage(), e);
            throw new RuntimeException("Error fetching current weather: " + e.getMessage());
        }
    }

    @McpTool(name = "get_weather_forecast", description = "Retrieves the 5-day weather forecast (with 3-hour steps) for a specific city.")
    public ForecastWeather getWeatherForecast(
            @McpToolParam(description = "The city name (e.g. London, Tokyo)", required = true) String city,
            @McpToolParam(description = "Two-letter ISO country code (e.g. US, GB, JP) to make search precise", required = false) String countryCode,
            @McpToolParam(description = "Temperature units: 'metric' (default, Celsius), 'imperial' (Fahrenheit), or 'standard' (Kelvin)", required = false) String units) {

        String unitParam = (units == null || units.isBlank()) ? "metric" : units;
        String query = (countryCode == null || countryCode.isBlank()) ? city : city + "," + countryCode;

        logger.info("Fetching forecast for: {}, units: {}", query, unitParam);

        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("q", query)
                            .queryParam("appid", apiKey)
                            .queryParam("units", unitParam)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                throw new RuntimeException("Empty response from OpenWeather API");
            }

            Map<String, Object> cityInfo = (Map<String, Object>) response.get("city");
            String name = cityInfo != null ? (String) cityInfo.get("name") : city;
            String country = cityInfo != null ? (String) cityInfo.get("country") : "";

            List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
            List<ForecastItem> forecastItems = new ArrayList<>();

            if (list != null) {
                for (Map<String, Object> item : list) {
                    String dtTxt = (String) item.get("dt_txt");
                    Map<String, Object> main = (Map<String, Object>) item.get("main");
                    Map<String, Object> wind = (Map<String, Object>) item.get("wind");
                    List<Map<String, Object>> weatherList = (List<Map<String, Object>>) item.get("weather");

                    String desc = "Unknown";
                    if (weatherList != null && !weatherList.isEmpty()) {
                        desc = (String) weatherList.get(0).get("description");
                    }

                    double temp = main != null ? ((Number) main.get("temp")).doubleValue() : 0.0;
                    double feelsLike = main != null ? ((Number) main.get("feels_like")).doubleValue() : 0.0;
                    int humidity = main != null ? ((Number) main.get("humidity")).intValue() : 0;
                    double windSpeed = wind != null ? ((Number) wind.get("speed")).doubleValue() : 0.0;

                    forecastItems.add(new ForecastItem(dtTxt, temp, feelsLike, humidity, desc, windSpeed));
                }
            }

            return new ForecastWeather(name, country, forecastItems);

        } catch (Exception e) {
            logger.error("Failed to fetch weather forecast for {}: {}", query, e.getMessage(), e);
            throw new RuntimeException("Error fetching weather forecast: " + e.getMessage());
        }
    }
}
