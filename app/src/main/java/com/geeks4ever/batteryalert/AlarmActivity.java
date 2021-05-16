/*
 * Created by Praveen Kumar for BatteryAlert.
 * Copyright (c) 2021.
 * Last modified on 17/5/21 12:14 AM.
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

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.material.textview.MaterialTextView;

public class AlarmActivity extends AppCompatActivity {

    Repository repository;
    Uri alarmSound;
    MediaPlayer mp;
    MaterialTextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        message = findViewById(R.id.alarm_page_message_text);

        repository = Repository.getInstance(this);
        repository.setAlarmIsOn(true);

        repository.getBatteryPercentage().observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer != null)
                    message.setText("Battery Reached "+ String.valueOf(integer) +"%!\nPlease unplug the charger.");
            }
        });

        repository.getAlarmIsOn().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean != null && !aBoolean)
                    finish();
            }
        });

        alarmSound = RingtoneManager. getDefaultUri (RingtoneManager.TYPE_ALARM );
        mp = MediaPlayer. create (getApplicationContext(), alarmSound);
        mp.start();

    }


    public void stopAlarm(View view){
        finish();
    }

    @Override
    protected void onDestroy() {
        mp.stop();
        repository.setAlarmIsOn(false);
        super.onDestroy();
    }
}