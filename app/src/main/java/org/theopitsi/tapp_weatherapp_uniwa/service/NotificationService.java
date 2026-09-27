package org.theopitsi.tapp_weatherapp_uniwa.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import android.os.Binder;
import androidx.core.app.NotificationCompat;

import org.theopitsi.tapp_weatherapp_uniwa.MainActivity;

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "weather_alert_channel";
    private static final int NOTIF_ID = 2001;
    private static final long DELAY_MS = 5 * 1000; //5 seconds

    Handler serviceHandler;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceHandler = new Handler();
        createNotificationChannel();
        System.out.println("*** NotificationService: onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("*** NotificationService: onStartCommand — scheduling alert in 5s");

        //schedule notification to fire after 5 seconds for DEMO
        serviceHandler.postDelayed(() -> {
            sendDEBUGWeatherNotification();
            stopSelf(); //done. kapootl. bam
        }, DELAY_MS);

        // must press button again, service doesnt revive
        return START_NOT_STICKY;
    }

    private void sendDEBUGWeatherNotification() {
        sendStaticNotification(this,"!!! Extreme weather condition !!!", "Big bad weather in your area!");
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Notifications for extreme weather conditions");
        channel.enableVibration(true); //HAHHAHAHA

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceHandler.removeCallbacksAndMessages(null);
        System.out.println("onDestroy");
    }
    private final IBinder Binder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        NotificationService getService()
        {
            return NotificationService.this;
        }
    }
    @Override
    public IBinder onBind (Intent intent)
    {
        ShowMessage("*** NotificationService - Someone binded");
        return Binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        ShowMessage("*** NotificationService -Someone Unbinded");
        return false;
    }

    private void ShowMessage(String Mess)
    {
        Toast Tst = Toast.makeText(getApplicationContext (), "Service: " + Mess, Toast.LENGTH_LONG);
        Tst.show();
    }

    //static notif method for others to use
    public static void sendStaticNotification(Context context, String title, String text) {
        //Open APP!!!
        Intent openApp = new Intent(context, MainActivity.class);
        //create an intent to open the app so we can actually req for it
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openApp, PendingIntent.FLAG_IMMUTABLE);

        //build the notif to send
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // dismiss when tapped

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIF_ID, builder.build());

        System.out.println("*** NotificationService - notification sent");
    }
}