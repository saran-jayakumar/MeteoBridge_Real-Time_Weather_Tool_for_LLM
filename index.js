import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import axios from "axios";
import dotenv from "dotenv";

dotenv.config();

const server = new Server(
{
name:"weather-mcp",
version:"1.0.0"
},
{
capabilities:{
tools:{}
}
}
);

// List Tools
server.setRequestHandler(
"tools/list",
async()=>({

tools:[
{
name:"weather",
description:"Get weather by city",

inputSchema:{
type:"object",
properties:{
city:{
type:"string"
}
},
required:["city"]
}

}

]

})
);

// Execute Tool
server.setRequestHandler(
"tools/call",
async(req)=>{

if(req.params.name==="weather"){

const city =
req.params.arguments.city;

const result =
await axios.get(

`https://api.openweathermap.org/data/2.5/weather?q=${city}&appid=${process.env.API_KEY}&units=metric`

);

const data=result.data;

return{

content:[
{
type:"text",

text:
`
City: ${data.name}

Temperature:
${data.main.temp}°C

Weather:
${data.weather[0].main}

Humidity:
${data.main.humidity}%
`
}

]

};

}

}
);

console.log(
"Weather MCP Running"
);