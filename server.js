import axios from "axios";
import dotenv from "dotenv";
import readline from "readline";

dotenv.config();

const rl = readline.createInterface({
input: process.stdin,
output: process.stdout
});

async function getWeather(city){

try{

const response =
await axios.get(
"https://api.openweathermap.org/data/2.5/weather",
{
params:{
q: city,
appid: process.env.API_KEY,
units:"metric"
}
}
);

const data =
response.data;

console.log(`
====================

🌍 City:
${data.name}

🌡 Temperature:
${data.main.temp}°C

☁ Weather:
${data.weather[0].description}

💧 Humidity:
${data.main.humidity}%

🌬 Wind:
${data.wind.speed} m/s

====================
`);

}
catch(error){

console.log(
"\n❌ City not found or API error"
);

}

rl.close();

}

rl.question(
"Enter City Name: ",
(city)=>{

getWeather(city);

});