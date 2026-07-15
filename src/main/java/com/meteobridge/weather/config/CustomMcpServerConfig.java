package com.meteobridge.weather.config;

import com.meteobridge.weather.service.WeatherService;
import com.meteobridge.weather.model.CurrentWeather;
import com.meteobridge.weather.model.ForecastWeather;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStreamableServerTransportProvider;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Configuration
public class CustomMcpServerConfig {

    @Bean
    public WebMvcStreamableServerTransportProvider webMvcStreamableServerTransportProvider() {
        if (System.getenv("PORT") != null) {
            // Instantiate Jackson 3's JsonMapper manually inside this method (so it is not registered as a bean,
            // and Spring's reflection post-processors won't scan it and crash on Java 25!)
            JsonMapper jsonMapper = new JsonMapper();
            McpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(jsonMapper);
            return WebMvcStreamableServerTransportProvider.builder()
                .jsonMapper(mcpJsonMapper)
                .mcpEndpoint("/mcp/message")
                .build();
        }
        return null;
    }

    @Bean
    public RouterFunction<ServerResponse> mcpRouterFunction(ObjectProvider<WebMvcStreamableServerTransportProvider> sseTransportProvider) {
        WebMvcStreamableServerTransportProvider transport = sseTransportProvider.getIfAvailable();
        if (transport != null) {
            return transport.getRouterFunction();
        }
        return null;
    }

    @Bean
    public McpSyncServer mcpSyncServer(WeatherService weatherService, ObjectProvider<WebMvcStreamableServerTransportProvider> sseTransportProvider) {
        WebMvcStreamableServerTransportProvider sseTransport = sseTransportProvider.getIfAvailable();

        // Instantiate standard Jackson 2 ObjectMapper for serialization of return types
        ObjectMapper objectMapper = new ObjectMapper();

        // Define schema for get_current_weather
        Map<String, Object> getCurrentWeatherSchema = Map.of(
            "type", "object",
            "properties", Map.of(
                "city", Map.of("type", "string", "description", "The city name (e.g. London, Tokyo)"),
                "countryCode", Map.of("type", "string", "description", "Two-letter ISO country code (e.g. US, GB, JP) to make search precise"),
                "units", Map.of("type", "string", "description", "Temperature units: 'metric' (default, Celsius), 'imperial' (Fahrenheit), or 'standard' (Kelvin)")
            ),
            "required", List.of("city")
        );

        // Define Tool for get_current_weather using builder
        McpSchema.Tool getCurrentWeatherTool = McpSchema.Tool.builder()
            .name("get_current_weather")
            .description("Retrieves the current weather information for a specific city.")
            .inputSchema(getCurrentWeatherSchema)
            .build();

        // Create SyncToolSpecification for get_current_weather
        SyncToolSpecification getCurrentWeatherSpec = SyncToolSpecification.builder()
            .tool(getCurrentWeatherTool)
            .callHandler((exchange, request) -> {
                try {
                    Map<String, Object> arguments = request.arguments();
                    String city = (String) arguments.get("city");
                    String countryCode = (String) arguments.get("countryCode");
                    String units = (String) arguments.get("units");
                    CurrentWeather weather = weatherService.getCurrentWeather(city, countryCode, units);
                    String json = objectMapper.writeValueAsString(weather);
                    return McpSchema.CallToolResult.builder()
                        .content(List.<McpSchema.Content>of(new McpSchema.TextContent(json)))
                        .isError(false)
                        .build();
                } catch (Exception e) {
                    return McpSchema.CallToolResult.builder()
                        .content(List.<McpSchema.Content>of(new McpSchema.TextContent("Error: " + e.getMessage())))
                        .isError(true)
                        .build();
                }
            })
            .build();

        // Define schema for get_weather_forecast
        Map<String, Object> getWeatherForecastSchema = Map.of(
            "type", "object",
            "properties", Map.of(
                "city", Map.of("type", "string", "description", "The city name (e.g. London, Tokyo)"),
                "countryCode", Map.of("type", "string", "description", "Two-letter ISO country code (e.g. US, GB, JP) to make search precise"),
                "units", Map.of("type", "string", "description", "Temperature units: 'metric' (default, Celsius), 'imperial' (Fahrenheit), or 'standard' (Kelvin)")
            ),
            "required", List.of("city")
        );

        // Define Tool for get_weather_forecast using builder
        McpSchema.Tool getWeatherForecastTool = McpSchema.Tool.builder()
            .name("get_weather_forecast")
            .description("Retrieves the 5-day weather forecast (with 3-hour steps) for a specific city.")
            .inputSchema(getWeatherForecastSchema)
            .build();

        // Create SyncToolSpecification for get_weather_forecast
        SyncToolSpecification getWeatherForecastSpec = SyncToolSpecification.builder()
            .tool(getWeatherForecastTool)
            .callHandler((exchange, request) -> {
                try {
                    Map<String, Object> arguments = request.arguments();
                    String city = (String) arguments.get("city");
                    String countryCode = (String) arguments.get("countryCode");
                    String units = (String) arguments.get("units");
                    ForecastWeather forecast = weatherService.getWeatherForecast(city, countryCode, units);
                    String json = objectMapper.writeValueAsString(forecast);
                    return McpSchema.CallToolResult.builder()
                        .content(List.<McpSchema.Content>of(new McpSchema.TextContent(json)))
                        .isError(false)
                        .build();
                } catch (Exception e) {
                    return McpSchema.CallToolResult.builder()
                        .content(List.<McpSchema.Content>of(new McpSchema.TextContent("Error: " + e.getMessage())))
                        .isError(true)
                        .build();
                }
            })
            .build();

        // Overloaded builder compilation handles each transport type (McpStreamableServerTransportProvider or McpServerTransportProvider) separately
        if (System.getenv("PORT") != null && sseTransport != null) {
            return McpServer.sync(sseTransport)
                    .serverInfo("weather-mcp-server", "1.0.0")
                    .capabilities(ServerCapabilities.builder()
                            .tools(true)
                            .build())
                    .tools(getCurrentWeatherSpec, getWeatherForecastSpec)
                    .build();
        } else {
            // Instantiate Jackson 3's JsonMapper manually inside this method
            JsonMapper jsonMapper = new JsonMapper();
            McpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(jsonMapper);
            StdioServerTransportProvider stdioTransport = new StdioServerTransportProvider(mcpJsonMapper);
            
            return McpServer.sync(stdioTransport)
                    .serverInfo("weather-mcp-server", "1.0.0")
                    .capabilities(ServerCapabilities.builder()
                            .tools(true)
                            .build())
                    .tools(getCurrentWeatherSpec, getWeatherForecastSpec)
                    .build();
        }
    }
}
