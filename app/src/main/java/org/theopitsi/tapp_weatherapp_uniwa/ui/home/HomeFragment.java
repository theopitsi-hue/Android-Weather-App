package org.theopitsi.tapp_weatherapp_uniwa.ui.home;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import org.theopitsi.tapp_weatherapp_uniwa.R;
import org.theopitsi.tapp_weatherapp_uniwa.data.WeatherCodes;
import org.theopitsi.tapp_weatherapp_uniwa.data.HourlyWeatherData;
import org.theopitsi.tapp_weatherapp_uniwa.data.WeekWeatherData;
import org.theopitsi.tapp_weatherapp_uniwa.databinding.FragmentHomeBinding;
import org.theopitsi.tapp_weatherapp_uniwa.databinding.ViewForecastDayBinding;
import org.theopitsi.tapp_weatherapp_uniwa.service.WeatherFetchService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements LocationListener {

    //-- ui variables
    private TextView displayResultLabel;
    private TextView rainChanceLabel;
    private TextView humidityLabel;
    private TextView weatherSituationLabel;
    private TextView dayDataLabel;
    private TextView fancyAddrLabel;
    private TextView temperatureLabel;
    private TextView temperatureFeelsLikeLabel;
    private TextView tempUnitLabel;
    private EditText inputFieldLocation;
    private ImageView weatherImage;

    //-- Operational variables
    private FragmentHomeBinding binding;
    private LocationManager locMan;
    HourlyWeatherData curWeatherData = new HourlyWeatherData();
    private double lat;
    private double lon;
    private String fancyAddrText;
    private int units = 0; //0 for C, 1 for F
    private boolean avgWeekday = false;//if the ui should display avg temp instead of minmax

    //-- service binding
    private WeatherFetchService weatherService;
    private boolean isBound = false;
    //NEEDS to be a variable cos i need to bind/unbind it explicitly
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WeatherFetchService.LocalBinder localBinder = (WeatherFetchService.LocalBinder) service;
            weatherService = localBinder.getService();
            isBound = true;

            weatherService.forceUpdate(requireContext());

            //SET UP CALLBACK SO WE UPDATE THE UI AAAAAAAAAAAAAAAAAAAAAAAA
            WeatherFetchService.getWeatherData().observe(getViewLifecycleOwner(), data -> {
                updateWeatherView(data);
            });

            WeatherFetchService.getWeekWeatherData().observe(getViewLifecycleOwner(), data -> {
                updateWeekView(data);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), WeatherFetchService.class);
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            requireActivity().unbindService(connection);
            isBound = false;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //grab fields from ui
        displayResultLabel = binding.tvResult;
        inputFieldLocation = binding.etLocation;
        Button btnGps = binding.btnGps;
        Button btnSearch = binding.btnSearch;
        fancyAddrLabel = binding.placeFancyText;
        rainChanceLabel = binding.rainChance;
        humidityLabel = binding.humidity;
        weatherSituationLabel = binding.weatherSituation;
        dayDataLabel = binding.dayData;
        temperatureLabel= binding.temperature;
        temperatureFeelsLikeLabel= binding.tempFeelsLike;
        tempUnitLabel= binding.tempUnit;
        weatherImage = binding.weatherImage;

        //recover data from prefs
        SharedPreferences prefs = requireContext().getSharedPreferences("weather_prefs",Context.MODE_PRIVATE);
        setLatLong(prefs.getFloat("last_lat",200),prefs.getFloat("last_lon",200));
        units = prefs.getInt("temp_units",0);
        avgWeekday = prefs.getBoolean("use_avg_week",false);

        //auto locationm getter
        btnGps.setOnClickListener(v -> requestCurrentLocation());

        //turns text into geocode (so cool)
        btnSearch.setOnClickListener(v -> geocodeInput());

        inputFieldLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                geocodeInput();
                return true;
            }
            return false;
        });

        //for notifications and fine location
        if (requireActivity().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        return root;
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            displayResultLabel.setText("No fine location permission!"); //didnt we ask for it above? maybe was denied :(
            return;
        }

        locMan = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        //why is this deprecated if its fine on sample projects? android vers?
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        //works fine so just use anyway
        String provider = locMan.getBestProvider(criteria, true);

        if (provider == null) {
            displayResultLabel.setText("No location provider available.\n Please enable GPS.");
            return;
        }

        displayResultLabel.setText("Detecting location...");

        try {
            Location last = locMan.getLastKnownLocation(provider);

            if (last != null) {
                onLocationChanged(last);
            }else{
                //probably hasnt updated?
                displayResultLabel.setText("No last known location...");
            }

            locMan.requestLocationUpdates(provider, 3000, 2.5f, this);
        } catch (SecurityException e) {
            displayResultLabel.setText("Permission ERROR: " + e.getMessage());
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        setLatLong(location.getLatitude(),location.getLongitude());

        if (locMan != null) {
            locMan.removeUpdates(this);
            //basically just receives one location update and dips out insta, aka detecting only current loc
        }
    }


    //Turns the text input on the location ui to a lat/long to use when pulling temperature
    private void geocodeInput() {
        //input field
        String query = inputFieldLocation.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "You have to enter location!", Toast.LENGTH_SHORT).show();
            return;
        }

        displayResultLabel.setText("Searching...");

        //search off the main thread so it doesn't stall and crash
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            try {
                List<Address> results = geocoder.getFromLocationName(query, 1);

                requireActivity().runOnUiThread(() -> {
                    if (results != null && !results.isEmpty()) {
                        Address addr = results.get(0); //find first address

                        //debug
                        //showResult(addr.getLatitude(), addr.getLongitude(), addr.getAddressLine(0));

                        //set lat/long
                        setLatLong(addr.getLatitude(),addr.getLongitude());
                    } else {
                        displayResultLabel.setText("Location not found.");
                    }
                });
            } catch (IOException e) {
                //i sure hope not
                requireActivity().runOnUiThread(() -> displayResultLabel.setText("Geocoding ERROORRRR: " + e.getMessage()));
            }
        }).start();
    }

    //sets and saves the latlong in preferences. Instantly updates weather data.
    private void setLatLong(double latitude, double longitude) {
        if (latitude >= 200 || longitude >= 200){
            //invalid lat or long recovered from prefs!
            //auto fetching...
            requestCurrentLocation();
            return;
        }

        this.lat = latitude;
        this.lon = longitude;

        String coords = String.format(Locale.getDefault(), "Lat: %.5f Lon: %.5f", lat, lon);
        displayResultLabel.setText(coords);

        //better formatting, get address
        reverseGeocode(lat, lon);

        //api call (OLD)
//        new Thread(() -> {
//            curWeatherData = WeatherApiRepo.fetch(lat,lon);
//
//            updateWeatherView();
//        }).start();

        //save data so it persists between app openings
        SharedPreferences prefs = requireContext().getSharedPreferences("weather_prefs",Context.MODE_PRIVATE);

        //will this cause issues? cos its floats?
        prefs.edit()
                .putFloat("last_lat", (float) lat)
                .putFloat("last_lon", (float) lon)
                .apply();

        if (isBound) {
            weatherService.forceUpdate(requireContext());
//            curWeatherData = weatherService.getLastWeatherData();
//            updateWeatherView(data);
        }
    }

    //updates the Weather Ui with all the data from the api
    private void updateWeatherView(HourlyWeatherData data){
        curWeatherData = data;

        requireActivity().runOnUiThread(() -> {
            if (curWeatherData == null) return;

            fancyAddrLabel.setText(fancyAddrText);

            var weatherStatus = WeatherCodes.get(curWeatherData.getWeather_code(),curWeatherData.isDay());
            weatherSituationLabel.setText(weatherStatus.getDescription());
            weatherImage.setImageResource(weatherStatus.getImageId());

            dayDataLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM")));
            tempUnitLabel.setText(getTempUnit());

            temperatureLabel.setText(getString(R.string.temperature,adjustToUnits(curWeatherData.getTemperature())));
            temperatureFeelsLikeLabel.setText(getString(R.string.feels_like, adjustToUnits(curWeatherData.getTempFeelsLike()), getTempUnit()));

            humidityLabel.setText(getString(R.string.humidity, curWeatherData.getHumidity()));

            rainChanceLabel.setText(getString(R.string.precipitation, curWeatherData.getRainChance()));
        });
    }

    //updates the Week Ui with all the data from the api
    private void updateWeekView(WeekWeatherData data) {
        requireActivity().runOnUiThread(() -> {
            setForecastDay(binding.day1,
                    data.getDays()[0],
                    data.getTempsMin()[0],
                    data.getTempsMax()[0],
                    data.getWeatherCodes()[0]);

            setForecastDay(binding.day2,
                    data.getDays()[1],
                    data.getTempsMin()[1],
                    data.getTempsMax()[1],
                    data.getWeatherCodes()[1]);

            setForecastDay(binding.day3,
                    data.getDays()[2],
                    data.getTempsMin()[2],
                    data.getTempsMax()[2],
                    data.getWeatherCodes()[2]);

            setForecastDay(binding.day4,
                    data.getDays()[3],
                    data.getTempsMin()[3],
                    data.getTempsMax()[3],
                    data.getWeatherCodes()[3]);

            setForecastDay(binding.day5,
                    data.getDays()[4],
                    data.getTempsMin()[4],
                    data.getTempsMax()[4],
                    data.getWeatherCodes()[4]);

            setForecastDay(binding.day6,
                    data.getDays()[5],
                    data.getTempsMin()[5],
                    data.getTempsMax()[5],
                    data.getWeatherCodes()[5]);

            setForecastDay(binding.day7,
                    data.getDays()[6],
                    data.getTempsMin()[6],
                    data.getTempsMax()[6],
                    data.getWeatherCodes()[6]);
        });
    }

    //helper for filling each weekday in the ui
    private void setForecastDay(ViewForecastDayBinding dayBinding, String day, float min, float max, int weatherCode) {
        dayBinding.dayLabel.setText(day);
        if (!avgWeekday) {
            dayBinding.tempLabel.setText(String.format("%.0f° - %.0f°", adjustToUnits(min), adjustToUnits(max)));
        }else{
            dayBinding.tempLabel.setText(String.format("~%.0f°", adjustToUnits((min+max)/2f)));
        }
        var weatherStatus = WeatherCodes.get(weatherCode,true);
        dayBinding.weatherIcon.setImageResource(weatherStatus.getImageId());
    }

    private String getTempUnit(){
        return (units == 0) ? "C" : "F";
    }

    //just sets a fancy address for the ui to display
    private void setAddress(Address address) {
        String area = address.getAdminArea();
        String locality = address.getLocality();
        String country = address.getCountryName();

        String location;

        if (area != null && !area.isBlank()) {
            location = area;
        } else if (locality != null && !locality.isBlank()) {
            location = locality;
        } else {
            location = "";
        }

        if (!location.isEmpty()) {
            fancyAddrText = location + ", " + country;
        } else {
            fancyAddrText = country;
        }
    }

    //from lat long -> location!
    private void reverseGeocode(double lat, double lng) {
        new Thread(() -> {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

            String result;
            String address = "";

            try {
                List<Address> list = geocoder.getFromLocation(lat, lng, 1);

                if (list != null && !list.isEmpty()) {
                    //first address found
                    Address addr = list.get(0);
                    StringBuilder sb = new StringBuilder();
                    StringBuilder aa = new StringBuilder();

                    for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                        sb.append(addr.getAddressLine(i)).append("\n");
                        aa.append(addr.getAddressLine(i)).append(",");
                    }

                    address = aa.toString().trim();
                    address = address.substring(0, address.length() - 1);
                    setAddress(addr);

                    sb.append(String.format(Locale.getDefault(), "Lat: %.5f  Lon: %.5f", lat, lng));
                    result = sb.toString().trim();

                } else {
                    result = String.format(Locale.getDefault(),"Lat: %.5f\nLon: %.5f", lat, lng);
                }

            } catch (IOException e) {
                result = String.format(Locale.getDefault(), "Lat: %.5f\nLon: %.5f", lat, lng);

            }

            String finalResult = result;
            String finalAddress = address;
            requireActivity().runOnUiThread(() -> {
                displayResultLabel.setText(finalResult);
                if (!finalAddress.isEmpty()) {
                    inputFieldLocation.setText(finalAddress);
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        if (locMan != null) {
            locMan.removeUpdates(this);
        }

        binding = null;
    }

    public float adjustToUnits(float celsius) {
        if (units == 0){
            return celsius;
        }
        return (celsius * 9f / 5f) + 32f;
    }
}