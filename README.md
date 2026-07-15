# OpenWeather Real-Time Weather MCP Server for LLMs

A Model Context Protocol (MCP) server that provides real-time weather information and 5-day weather forecasts to LLMs (like Claude Desktop). Built with **Java Spring Boot** and the official **Spring AI MCP SDK**.

It features a **dual-transport architecture**:
1. **STDIO Transport**: Runs as a local subprocess, ideal for local use with Claude Desktop.
2. **SSE (Server-Sent Events) Transport**: Runs as a standard HTTP Web Service, ideal for hosting in the cloud (such as on Render.com).

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
- `server.port`: Configured to dynamically bind to `${PORT:8080}` (required for cloud hosting).

> [!WARNING]
> **API Key Safety**: Do not commit your OpenWeather API key to public GitHub repositories. Keep it in your environment variables or remove any hardcoded fallbacks in `application.properties` before committing.

---

## How to Build and Run Locally (STDIO)

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

## Cloud Deployment (Render.com)

You can host your MCP server in the cloud as an HTTP Web Service on Render.com using the built-in Dockerfile. The application will automatically detect the `PORT` environment variable set by Render and switch to the **SSE (Server-Sent Events) Transport** listening on that port.

### Step-by-Step Deploy to Render:

1. **Push your code to GitHub** (make sure your repo is public or accessible to Render).
2. Go to the [Render Dashboard](https://dashboard.render.com/) and click **New +** -> **Web Service**.
3. **Connect your GitHub repository** `MeteoBridge_Real-Time_Weather_Tool_for_LLM`.
4. In the Web Service configuration:
   - **Name**: `weather-mcp-server` (or any custom name)
   - **Region**: Select the closest region to you
   - **Branch**: `main`
   - **Runtime**: Select **Docker**
   - **Instance Type**: Select the **Free** tier
5. Click **Advanced** and add an Environment Variable:
   - **Key**: `OPENWEATHER_API_KEY`
   - **Value**: `YOUR_ACTUAL_OPENWEATHER_API_KEY`
6. Click **Create Web Service** to trigger the deployment.

Once deployed, Render will provide you with a public URL, for example: `https://weather-mcp-server-xxxx.onrender.com`.

---

## Connecting to Your Render Server (SSE Client)

Once your server is hosted on Render, anyone can connect their Claude Desktop to your server via the network over SSE without needing Docker or Java locally.

Add the following to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "weather-mcp-server": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/client-sse",
        "https://YOUR-RENDER-SERVICE-NAME.onrender.com/mcp/sse"
      ]
    }
  }
}
```

*(Replace `https://YOUR-RENDER-SERVICE-NAME.onrender.com` with your actual Render deployment URL).*

---

## Containerized Local Deployment (Docker)

### 1. Build the Docker Image
```bash
docker build -t your-username/weather-mcp-server .
```

### 2. Run the Container (STDIO)
```bash
docker run -i --rm -e OPENWEATHER_API_KEY="your_openweather_api_key_here" your-username/weather-mcp-server
```
