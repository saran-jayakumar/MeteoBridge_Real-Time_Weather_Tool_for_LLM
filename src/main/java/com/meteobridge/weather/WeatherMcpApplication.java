package com.meteobridge.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    org.springframework.ai.mcp.server.common.autoconfigure.McpServerAutoConfiguration.class,
    org.springframework.ai.mcp.server.common.autoconfigure.McpServerJsonMapperAutoConfiguration.class
})
public class WeatherMcpApplication {
    public static void main(String[] args) {
        String port = System.getenv("PORT");
        System.err.println("DEBUG STARTUP: PORT environment variable is: " + port);
        SpringApplication app = new SpringApplication(WeatherMcpApplication.class);
        if (port != null) {
            System.err.println("DEBUG STARTUP: Starting as SERVLET web application...");
            app.setWebApplicationType(WebApplicationType.SERVLET);
        } else {
            System.err.println("DEBUG STARTUP: Starting as NONE console application...");
            app.setWebApplicationType(WebApplicationType.NONE);
        }
        app.run(args);
    }
}
