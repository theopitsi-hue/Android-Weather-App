package org.theopitsi.tapp_weatherapp_uniwa.data;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherApiRepo {

    //fetches the current weather in the specified lat and lon
    //since its a http request, should run in diff thread
    public static HourlyWeatherData fetch(double lat, double lon) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m"
                + "&timezone=auto&forecast_days=1";

        Request request = new Request.Builder().url(url).build();
        HourlyWeatherData data = null;

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            JSONObject root = new JSONObject(json);

            if (root.has("error")) {
                if (root.has("reason")) {
                    String reason = root.getString("reason");
                    //bad variables passed in or some other error, better just write it out
                    System.out.println("API FETCH ERROR: " + root.getString("reason"));
                }
                System.out.println(json);
                return null;
            }

            var cr = root.getJSONObject("current");

            data = new HourlyWeatherData(
                    (float) cr.getDouble("temperature_2m"),
                    0,
                    (float) cr.getDouble("relative_humidity_2m"),
                    (float) cr.getDouble("precipitation"),
                    (float) cr.getDouble("apparent_temperature"),
                    cr.getInt("weather_code"),
                    cr.getInt("is_day") != 0,
                    (float) cr.getDouble("wind_speed_10m")
            );

        } catch (Exception e) {
            System.out.println("API FETCH Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    //fetches the entire next week's weather (including today)
    //since its a http request, should run in diff thread
    public static WeekWeatherData fetchWeek(double lat, double lon) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&daily=weather_code,temperature_2m_min,temperature_2m_max"
                + "&forecast_days=7"
                + "&timezone=auto";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {

            String json = response.body().string();
            JSONObject root = new JSONObject(json);

            if (root.has("error")) {
                System.out.println(json);
                return null;
            }

            JSONObject daily = root.getJSONObject("daily");

            JSONArray minArray = daily.getJSONArray("temperature_2m_min");
            JSONArray maxArray = daily.getJSONArray("temperature_2m_max");
            JSONArray codeArray = daily.getJSONArray("weather_code");
            JSONArray dateArray = daily.getJSONArray("time");

            int days = minArray.length();

            float[] mins = new float[days];
            float[] maxs = new float[days];
            int[] codes = new int[days];
            String[] dates = new String[days];

            for (int i = 0; i < days; i++) {
                mins[i] = (float) minArray.getDouble(i);
                maxs[i] = (float) maxArray.getDouble(i);
                codes[i] = codeArray.getInt(i);
                dates[i] = dateArray.getString(i);
            }

            return new WeekWeatherData(mins, maxs, codes, dates);

        } catch (Exception e) {
            System.out.println("API FETCH Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
