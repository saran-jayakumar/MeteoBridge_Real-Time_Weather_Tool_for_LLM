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
        SpringApplication app = new SpringApplication(WeatherMcpApplication.class);
        // If PORT is defined, run as a Web (Servlet) application (e.g. on Render).
        // Otherwise, run as a console (NONE) application (e.g. locally inside Claude Desktop).
        if (System.getenv("PORT") != null) {
            app.setWebApplicationType(WebApplicationType.SERVLET);
        } else {
            app.setWebApplicationType(WebApplicationType.NONE);
        }
        app.run(args);
    }
}
