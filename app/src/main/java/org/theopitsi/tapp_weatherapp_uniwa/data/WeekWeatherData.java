package org.theopitsi.tapp_weatherapp_uniwa.data;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

//tiny weather data class to parse the week's weather from the api req
public class WeekWeatherData {
   private float[] temps_min;
   private float[] temps_max;
   private int[] weather_codes;
   private String[] dates;
   private String[] days;

   public WeekWeatherData(){}

    public WeekWeatherData(float[] temps_min, float[] temps_max, int[] weather_codes, String[] dates) {
        this.temps_min = temps_min;
        this.temps_max = temps_max;
        this.weather_codes = weather_codes;
        this.dates = dates;

        days = new String[dates.length];
        //fill in the week for each date's name (mon/tue.ect)
        for (int i = 0; i < dates.length; i++) {
            LocalDate date = LocalDate.parse(dates[i]);
            days[i] = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        }
    }

    public float[] getTempsMin() {
        return temps_min;
    }

    public float[] getTempsMax() {
        return temps_max;
    }

    public int[] getWeatherCodes() {
        return weather_codes;
    }

    public String[] getDates() {
        return dates;
    }

    public String[] getDays() {
        return days;
    }
}
