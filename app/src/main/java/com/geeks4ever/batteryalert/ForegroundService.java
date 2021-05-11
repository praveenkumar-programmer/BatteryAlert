/*
 * Created by Praveen Kumar for BatteryAlert.
 * Copyright (c) 2021.
 * Last modified on 11/5/21 12:11 PM.
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
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class ForegroundService extends Service {
    private static final int NOTIFICATION_ID = 999;

    int battery_threshold = 73;
    Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                Intent batteryStatus = getApplication()
                        .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

                if(batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        == BatteryManager.BATTERY_STATUS_CHARGING){

                    if(batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                            == BatteryManager.BATTERY_PLUGGED_AC){

                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        float batteryPct = level * 100 / (float)scale;

                        Log.e("battery alert", "battery level (AC) : "+batteryPct);

                        if(batteryPct == battery_threshold)

                            startActivity(new Intent(ForegroundService.this, AlarmActivity.class));

                    }
                    else if(batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                            == BatteryManager.BATTERY_PLUGGED_USB){

                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        float batteryPct = level * 100 / (float)scale;

                        Log.e("battery alert", "battery level (USB) : "+batteryPct);

                        if(batteryPct == battery_threshold)

                            startActivity(new Intent(ForegroundService.this, AlarmActivity.class));
                    }

                }

            }
        }, 0, 1000);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent homePagePendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent closeButtonIntent = new Intent(this, CloseButtonReceiver.class);
        PendingIntent closeButtonPendingIntent = PendingIntent.getBroadcast(this, 0, closeButtonIntent,0);

        Notification notification = new NotificationCompat.Builder(this, "Foreground Service")
                .setContentTitle("Battery Alert")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(homePagePendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "stop", closeButtonPendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);


        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
