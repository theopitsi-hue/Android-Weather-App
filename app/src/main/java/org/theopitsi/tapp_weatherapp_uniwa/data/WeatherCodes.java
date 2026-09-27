package org.theopitsi.tapp_weatherapp_uniwa.data;

import androidx.annotation.Nullable;

import org.theopitsi.tapp_weatherapp_uniwa.R;

import java.util.HashMap;
import java.util.Map;

//Associates weather codes + current daytime status to descriptions and icons for the ui!
public class WeatherCodes {
    //for daytime icons
    private static final Map<Integer, WeatherCodeInfo> DAY = new HashMap<>();

    //night time icons
    private static final Map<Integer, WeatherCodeInfo> NIGHT = new HashMap<>();

    static { //ta xerakia moy...
        //icons+data pulled from https://openweathermap.org/api/weather-conditions#Weather-Condition-Codes-2

        DAY.put(0, new WeatherCodeInfo("Sunny",R.drawable.ico_01d));
        NIGHT.put(0, new WeatherCodeInfo("Clear", R.drawable.ico_01n));

        DAY.put(1, new WeatherCodeInfo("Mainly Sunny", R.drawable.ico_01d));
        NIGHT.put(1, new WeatherCodeInfo("Mainly Clear", R.drawable.ico_01n));

        DAY.put(2, new WeatherCodeInfo("Partly Cloudy", R.drawable.ico_02d));
        NIGHT.put(2, new WeatherCodeInfo("Partly Cloudy", R.drawable.ico_02n));

        DAY.put(3, new WeatherCodeInfo("Cloudy", R.drawable.ico_03d));
        NIGHT.put(3, new WeatherCodeInfo("Cloudy", R.drawable.ico_03n));

        DAY.put(45, new WeatherCodeInfo("Foggy", R.drawable.ico_50d));
        NIGHT.put(45, new WeatherCodeInfo("Foggy", R.drawable.ico_50n));

        DAY.put(48, new WeatherCodeInfo("Rime Fog", R.drawable.ico_50d));
        NIGHT.put(48, new WeatherCodeInfo("Rime Fog", R.drawable.ico_50n));

        DAY.put(51, new WeatherCodeInfo("Light Drizzle", R.drawable.ico_09d));
        NIGHT.put(51, new WeatherCodeInfo("Light Drizzle", R.drawable.ico_09n));

        DAY.put(53, new WeatherCodeInfo("Drizzle", R.drawable.ico_09d));
        NIGHT.put(53, new WeatherCodeInfo("Drizzle", R.drawable.ico_09n));

        DAY.put(55, new WeatherCodeInfo("Heavy Drizzle", R.drawable.ico_09d));
        NIGHT.put(55, new WeatherCodeInfo("Heavy Drizzle", R.drawable.ico_09n));

        DAY.put(56, new WeatherCodeInfo("Light Freezing Drizzle", R.drawable.ico_09d));
        NIGHT.put(56, new WeatherCodeInfo("Light Freezing Drizzle", R.drawable.ico_09n));

        DAY.put(57, new WeatherCodeInfo("Freezing Drizzle", R.drawable.ico_09d));
        NIGHT.put(57, new WeatherCodeInfo("Freezing Drizzle", R.drawable.ico_09n));

        DAY.put(61, new WeatherCodeInfo("Light Rain", R.drawable.ico_10d));
        NIGHT.put(61, new WeatherCodeInfo("Light Rain", R.drawable.ico_10n));

        DAY.put(63, new WeatherCodeInfo("Rain", R.drawable.ico_10d));
        NIGHT.put(63, new WeatherCodeInfo("Rain", R.drawable.ico_10n));

        DAY.put(65, new WeatherCodeInfo("Heavy Rain", R.drawable.ico_10d));
        NIGHT.put(65, new WeatherCodeInfo("Heavy Rain", R.drawable.ico_10n));

        DAY.put(66, new WeatherCodeInfo("Light Freezing Rain", R.drawable.ico_10d));
        NIGHT.put(66, new WeatherCodeInfo("Light Freezing Rain", R.drawable.ico_10n));

        DAY.put(67, new WeatherCodeInfo("Freezing Rain", R.drawable.ico_10d));
        NIGHT.put(67, new WeatherCodeInfo("Freezing Rain", R.drawable.ico_10n));

        DAY.put(71, new WeatherCodeInfo("Light Snow", R.drawable.ico_13d));
        NIGHT.put(71, new WeatherCodeInfo("Light Snow", R.drawable.ico_13n));

        DAY.put(73, new WeatherCodeInfo("Snow", R.drawable.ico_13d));
        NIGHT.put(73, new WeatherCodeInfo("Snow", R.drawable.ico_13n));

        DAY.put(75, new WeatherCodeInfo("Heavy Snow", R.drawable.ico_13d));
        NIGHT.put(75, new WeatherCodeInfo("Heavy Snow", R.drawable.ico_13n));

        DAY.put(77, new WeatherCodeInfo("Snow Grains", R.drawable.ico_13d));
        NIGHT.put(77, new WeatherCodeInfo("Snow Grains", R.drawable.ico_13n));

        DAY.put(80, new WeatherCodeInfo("Light Showers", R.drawable.ico_09d));
        NIGHT.put(80, new WeatherCodeInfo("Light Showers", R.drawable.ico_09n));

        DAY.put(81, new WeatherCodeInfo("Showers", R.drawable.ico_09d));
        NIGHT.put(81, new WeatherCodeInfo("Showers", R.drawable.ico_09n));

        DAY.put(82, new WeatherCodeInfo("Heavy Showers", R.drawable.ico_09d));
        NIGHT.put(82, new WeatherCodeInfo("Heavy Showers", R.drawable.ico_09n));

        DAY.put(85, new WeatherCodeInfo("Light Snow Showers", R.drawable.ico_13d));
        NIGHT.put(85, new WeatherCodeInfo("Light Snow Showers", R.drawable.ico_13n));

        DAY.put(86, new WeatherCodeInfo("Snow Showers", R.drawable.ico_13d));
        NIGHT.put(86, new WeatherCodeInfo("Snow Showers", R.drawable.ico_13n));

        DAY.put(95, new WeatherCodeInfo("Thunderstorm", R.drawable.ico_11d));
        NIGHT.put(95, new WeatherCodeInfo("Thunderstorm", R.drawable.ico_11n));

        DAY.put(96, new WeatherCodeInfo("Light Thunderstorms With Hail", R.drawable.ico_11d));
        NIGHT.put(96, new WeatherCodeInfo("Light Thunderstorms With Hail", R.drawable.ico_11n));

        DAY.put(99, new WeatherCodeInfo("Thunderstorm With Hail", R.drawable.ico_11d));
        NIGHT.put(99, new WeatherCodeInfo("Thunderstorm With Hail", R.drawable.ico_11n));
    }

    //fetches the icon and description based on the weathercode and daytime status
    public static WeatherCodeInfo get(int code, boolean isDay) {
        WeatherCodeInfo info = isDay ? DAY.get(code) : NIGHT.get(code);

        if (info == null) {
            return new WeatherCodeInfo("Unknown", R.drawable.ico_11d);
        }

        return info;
    }
}