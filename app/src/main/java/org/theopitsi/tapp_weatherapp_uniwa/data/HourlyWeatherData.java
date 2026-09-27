package org.theopitsi.tapp_weatherapp_uniwa.data;

//data class for responses from the current weather api
public class HourlyWeatherData {
    private float temperature = 0;
    private int units = 0;
    private float humidity = 0;
    private float rainChance = 0;
    private float tempFeelsLike = 0;
    private int weather_code = 0;
    private boolean isDay=false;
    private float windSpeed = 0;

    public HourlyWeatherData(){}
    public HourlyWeatherData(float temperature, int units, float humidity, float rainChance, float tempFeelsLike, int weather_code, boolean isDay, float windSpeed) {
        this.temperature = temperature;
        this.units = units;
        this.humidity = humidity;
        this.rainChance = rainChance;
        this.tempFeelsLike = tempFeelsLike;
        this.weather_code = weather_code;
        this.isDay = isDay;
        this.windSpeed = windSpeed;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getUnits() {
        return units;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getRainChance() {
        return rainChance;
    }

    public float getTempFeelsLike() {
        return tempFeelsLike;
    }

    public int getWeather_code() {
        return weather_code;
    }

    public boolean isDay() {
        return isDay;
    }

    public float getWindSpeed() {
        return windSpeed;
    }
}
