package org.theopitsi.tapp_weatherapp_uniwa.data;

//tiny data class to associate descriptions and img resources to weathercodes
public class WeatherCodeInfo {
    private final String description;
    private final int imageResource;

    public WeatherCodeInfo(String description, int imageUrl) {
        this.description = description;
        this.imageResource = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public int getImageId() {
        return imageResource;
    }
}
