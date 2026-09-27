package org.theopitsi.tapp_weatherapp_uniwa.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import org.theopitsi.tapp_weatherapp_uniwa.MainActivity;
import org.theopitsi.tapp_weatherapp_uniwa.data.WeatherApiRepo;
import org.theopitsi.tapp_weatherapp_uniwa.data.HourlyWeatherData;
import org.theopitsi.tapp_weatherapp_uniwa.data.WeekWeatherData;

public class WeatherFetchService extends Service {
    private static final String CHANNEL_ID = "weather_polling_channel";
    private static final int FOREGROUND_NOTIF_ID = 3001;

    //INTERVAL TO SELF-UUUPDATEEEE
    private static final long INTERVAL_MS = 30 * 60 * 1000; // 30 minutes

   // private static final long INTERVAL_MS = 5 * 1000; // 5 secs DEBUG

    //sent via Intent to control the service from outside
    public static final String FORCE_UPDATE = "FORCE_UPDATE";

    private Handler serviceHandler;
    private Runnable loopingRunnable;

    //this took me maybne more than the whole project combined btw
    private static final MutableLiveData<HourlyWeatherData> weatherData = new MutableLiveData<>();
    private static final MutableLiveData<WeekWeatherData> weekWeatherData = new MutableLiveData<>();


    @Override
    public void onCreate() {
        super.onCreate();
        serviceHandler = new Handler();

        loopingRunnable = new Runnable() {
            @Override
            public void run() {
                doFetch();
                serviceHandler.postDelayed(this, INTERVAL_MS); // reschedule
            }
        };

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(FOREGROUND_NOTIF_ID, buildForegroundNotification("Watching for extreme weather..."));

        if (intent != null) {
            if (FORCE_UPDATE.equals(intent.getAction())) {
                //force immediate update
                serviceHandler.removeCallbacks(loopingRunnable);
                serviceHandler.post(loopingRunnable);
            }
        } else {
            //normal run
            serviceHandler.removeCallbacks(loopingRunnable); //why did it take this long bro
            serviceHandler.post(loopingRunnable);
        }

        return START_STICKY;
    }

    //force an immediate update
    public void forceUpdate(Context context) {
        Intent intent = new Intent(context, WeatherFetchService.class);
        intent.setAction(FORCE_UPDATE);
        context.startService(intent);
    }

    private void doFetch() {
        new Thread(() -> {
            SharedPreferences prefs = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE);
            float lat = prefs.getFloat("last_lat", 200);
            float lon = prefs.getFloat("last_lon", 200);

            if (lat >= 200 || lon >= 200) {
                System.out.println("*** WeatherFetchService: no saved location, skipping fetch");
                return;
            }

            System.out.println("*** WeatherFetchService: fetching weather at " + lat + ", " + lon);
            HourlyWeatherData data = WeatherApiRepo.fetch(lat, lon);
            WeekWeatherData weekWeather = WeatherApiRepo.fetchWeek(lat, lon);

            if (data == null) {
                System.out.println("*** WeatherFetchService: fetch returned null");
                return;
            }

            getWeatherData().postValue(data);
            getWeekWeatherData().postValue(weekWeather);

            //alert if extreme
            if (isExtremeWeather(data)) {
                Intent notifIntent = new Intent(getApplicationContext(), NotificationService.class);
                startService(notifIntent);
            }

            // Update the persistent foreground notification with current temp
            updateForegroundNotification("monitoring active, temp:" + data.getTemperature() + "°C");

        }).start();
    }

    //literally go to sahara to trigger notif fr
    private boolean isExtremeWeather(HourlyWeatherData data) {
        return  data.getTemperature()>=35;
    }

    //so bound components can read the last fetched data
    public static MutableLiveData<HourlyWeatherData> getWeatherData() {
        return weatherData;
    }

    public static MutableLiveData<WeekWeatherData> getWeekWeatherData() {
        return weekWeatherData;
    }


    private void updateForegroundNotification(String text) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(FOREGROUND_NOTIF_ID, buildForegroundNotification(text));
    }

    private Notification buildForegroundNotification(String text) {
        Intent openApp = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, openApp, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Weather Monitor")
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Weather Monitor", NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Runs in background to watch for extreme weather");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceHandler.removeCallbacks(loopingRunnable);
        System.out.println("*** WeatherFetchService: destroyed");
    }

    private final IBinder Binder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        public WeatherFetchService getService()
        {
            return WeatherFetchService.this;
        }
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return Binder;
    }


}