# OpenWeather Real-Time Weather MCP Server for LLMs

A Model Context Protocol (MCP) server that provides real-time weather information and 5-day weather forecasts to LLMs (like Claude Desktop). Built with **Java Spring Boot** and the official **Spring AI MCP SDK**.

---

## Features

Exposes two tools to LLMs:
1. `get_current_weather`: Retrieves current weather conditions (temperature, wind, humidity, weather description) for a specific city.
2. `get_weather_forecast`: Retrieves a 5-day / 3-hour interval weather forecast (temperature, description, forecast timestamps) for a specific city.

---

## Requirements

* **Java**: JDK 21 or higher (fully tested and compatible with JDK 25)
* **Maven**: For compilation and dependency management
* **OpenWeather API Key**: Get a free API key from [OpenWeatherMap](https://openweathermap.org/)

---

## Configuration

The server configuration properties are located in `src/main/resources/application.properties`:
- `openweather.api.key`: The API key placeholder (loads from `OPENWEATHER_API_KEY` env variable).
- `logging.file.name`: Absolute log file destination to avoid stdout pollution.

> [!WARNING]
> **API Key Safety**: Do not commit your OpenWeather API key to public GitHub repositories. Keep it in your environment variables or remove any hardcoded fallbacks in `application.properties` before committing.

---

## How to Build and Run Locally

### 1. Build the JAR
Compile and package the application using Maven:
```bash
mvn clean package -DskipTests
```
This builds a runnable FAT jar at: `target/weather-mcp-server-1.0.0.jar`.

### 2. Configure Claude Desktop
Add the server configuration to your Claude Desktop config file:

* **Path**: `~/Library/Application Support/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "weather-mcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/saran_j/Desktop/MCP_Server/MeteoBridge_Real-Time_Weather_Tool_for_LLM/target/weather-mcp-server-1.0.0.jar"
      ],
      "env": {
        "OPENWEATHER_API_KEY": "your_openweather_api_key_here"
      }
    }
  }
}
```

Restart Claude Desktop, and the tools will appear in your prompt input field!

---

## Containerized Deployment (Docker)

To make it easy for others to run your MCP server without needing to install Java or Maven locally:

### 1. Build the Docker Image
```bash
docker build -t your-username/weather-mcp-server .
```

### 2. Run the Container
You can run the server locally using the standard input/output stream pipeline:
```bash
docker run -i --rm -e OPENWEATHER_API_KEY="your_openweather_api_key_here" your-username/weather-mcp-server
```

### 3. Claude Desktop Configuration using Docker
Others can integrate your server using Docker:
```json
{
  "mcpServers": {
    "weather-mcp-server": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "OPENWEATHER_API_KEY=your_openweather_api_key_here",
        "your-username/weather-mcp-server"
      ]
    }
  }
}
```

---

## Publishing the Server

### 1. Publishing to GitHub
1. Create a new public repository on GitHub.
2. Initialize Git, add files, commit, and push:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of Java Weather MCP Server"
   git branch -M main
   git remote add origin https://github.com/your-username/your-repo-name.git
   git push -u origin main
   ```

### 2. Publishing to Docker Hub
1. Log into your Docker Hub account in terminal:
   ```bash
   docker login
   ```
2. Build, tag, and push the image:
   ```bash
   docker build -t your-username/weather-mcp-server:1.0.0 .
   docker tag your-username/weather-mcp-server:1.0.0 your-username/weather-mcp-server:latest
   docker push your-username/weather-mcp-server --all-tags
   ```

### 3. Adding to MCP registries
Once published, you can submit your GitHub repository or Docker image to:
- The [Glama MCP Registry](https://glama.ai/mcp/registry)
- The official [Model Context Protocol GitHub list](https://github.com/modelcontextprotocol/servers)
- [mcpservers.org](https://mcpservers.org)
