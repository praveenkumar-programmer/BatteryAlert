/*
 * Created by Praveen Kumar for BatteryAlert.
 * Copyright (c) 2021.
 * Last modified on 17/5/21 2:31 PM.
 *
 * This file/part of BatteryAlert is OpenSource.
 *
 * BatteryAlert is a free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * BatteryAlert is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with BatteryAlert.
 * If not, see http://www.gnu.org/licenses/.
 */

package com.geeks4ever.batteryalert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;

import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {

    private static final int NOTIFICATION_ID = 999;

    Repository repository;

    Uri alarmSound;
    MediaPlayer mp;

    int battery_threshold = 100;
    volatile boolean alertOnUsbCharging = false;

    String battery_percentage = "0", chargeMode = "Not Charging";

    NotificationCompat.Builder builder;
    NotificationManager managerCompat;
    Notification notification;

    Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        alarmSound = RingtoneManager. getDefaultUri (RingtoneManager.TYPE_ALARM );
        mp = MediaPlayer. create (this, alarmSound);

        repository = Repository.getInstance(this);

        repository.getMainServiceOnOff().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean != null && !aBoolean)
                    stopSelf();
            }
        });

        repository.getBatteryThreshold().observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer != null)
                battery_threshold = integer;
            }
        });

        repository.getAlertOnUsbCharging().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean != null)
                alertOnUsbCharging = aBoolean;
            }
        });

        repository.getBatteryPercentage().observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer == null)
                    return;
                if(battery_percentage.equals(String.valueOf(integer)))
                    return;
                battery_percentage = String.valueOf(integer);
                updateNotificationText(battery_percentage + "% ( "+ chargeMode + " )\n" + "Alert on " + battery_threshold + "%");
            }
        });

        repository.getChargeType().observeForever(new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s == null)
                    return;

                if(s.equals(chargeMode))
                    return;

                chargeMode = s;
                updateNotificationText(battery_percentage + "% ( "+ chargeMode + " )\n" + "Alert on " + battery_threshold + "%");
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                Intent batteryStatus = getApplication()
                        .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

                if(batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        == BatteryManager.BATTERY_STATUS_CHARGING){

                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                    float batteryPct = level * 100 / (float)scale;

                    if(Integer.parseInt(battery_percentage) != Math.round(batteryPct))
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                repository.setBatteryPercentage(Math.round(batteryPct));
                            }
                        });

                    if(batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                            == BatteryManager.BATTERY_PLUGGED_AC){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                repository.setChargeType("AC adaptor");
                            }
                        });

                        if(batteryPct >= battery_threshold){
                            showAlert("Battery Reached " + batteryPct + "%");
                        }
                    }

                    else if(batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                            == BatteryManager.BATTERY_PLUGGED_USB){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                repository.setChargeType("USB connection");
                            }
                        });

                        if(batteryPct >= battery_threshold && alertOnUsbCharging) {
                            showAlert("Battery Reached " + batteryPct + "%");
                        }
                    }


                }

            }
        }, 0, 1000);

        managerCompat = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent homePagePendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent closeButtonIntent = new Intent(this, CloseButtonReceiver.class);
        PendingIntent closeButtonPendingIntent = PendingIntent.getBroadcast(this, 0, closeButtonIntent,0);

        //________________________________________________________________________________________;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "Battery Alert";
            CharSequence name = "Foreground Service";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.enableVibration(false);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            managerCompat.createNotificationChannel(mChannel);
        }

        //_________________________________________________________________________________________

        builder = new NotificationCompat.Builder(this, "Battery Alert");
        builder.setOngoing(true);

        notification = builder
                .setContentTitle("Battery Alert")
                .setSmallIcon(R.drawable.battery_icon)
                .setContentText(battery_percentage + "% ( "+ chargeMode + " )\n" + "Alert on " + battery_threshold + "%")
                .setContentIntent(homePagePendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(battery_percentage + "% ( "+ chargeMode + " )\n" + "Alert on " + battery_threshold + "%"))
                .addAction(R.drawable.ic_launcher_foreground, "stop", closeButtonPendingIntent)
                .build();

        managerCompat.notify(NOTIFICATION_ID, notification);

        startForeground(NOTIFICATION_ID, notification);


    }


    @Override
    public void onDestroy() {
        timer.cancel();
        mp.stop();
        managerCompat.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    void showAlert(String text){

        timer.cancel();
        mp.start();
        builder.setContentText(text)
                .setCategory(NotificationCompat.CATEGORY_ALARM);
        managerCompat.notify(NOTIFICATION_ID, builder.build());
    }


    void updateNotificationText (String text){

        if(builder == null || managerCompat == null)
            return;

        builder.setContentText(text);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        managerCompat.notify(NOTIFICATION_ID, builder.build());

    }

}
