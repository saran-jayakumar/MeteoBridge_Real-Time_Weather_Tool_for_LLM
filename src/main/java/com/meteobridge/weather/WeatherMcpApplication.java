package com.meteobridge.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    org.springframework.ai.mcp.server.common.autoconfigure.McpServerAutoConfiguration.class,
    org.springframework.ai.mcp.server.common.autoconfigure.McpServerJsonMapperAutoConfiguration.class
})
public class WeatherMcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeatherMcpApplication.class, args);
    }
}
