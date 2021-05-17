/*
 * Created by Praveen Kumar for BatteryAlert.
 * Copyright (c) 2021.
 * Last modified on 17/5/21 1:24 PM.
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

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Repository repository;
    Timer timer = new Timer();

    MaterialTextView batteryStatusText, batteryPercentageText;
    SwitchMaterial mainServiceSwitch, automationSwitch, alertForUsbSwitch;

    TextInputEditText batteryThresholdText;
    int batteryThreshold;

    MaterialButton minusButton, plusButton;

    FrameLayout automaticModeLayout;
    MaterialTextView automaticModeLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = Repository.getInstance(this);

        batteryStatusText = findViewById(R.id.home_page_charging_state_text);
        mainServiceSwitch = findViewById(R.id.home_page_battery_alert_on_off_switch);
        automationSwitch = findViewById(R.id.home_page_automatic_mode_on_off_switch);
        alertForUsbSwitch = findViewById(R.id.home_page_alert_for_usb_charging_too_switch);

        automaticModeLayout = findViewById(R.id.home_page_auto_mode_layout);
        automaticModeLabel = findViewById(R.id.home_page_automatic_mode_label);

        minusButton = findViewById(R.id.home_page_battery_threshold_minus_button);
        plusButton = findViewById(R.id.home_page_battery_threshold_plus_button);

        batteryPercentageText = findViewById(R.id.home_page_battery_percentage_text);
        batteryThresholdText = findViewById(R.id.battery_threshold_edit_text);

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(batteryThreshold <= 100 && batteryThreshold >= 1)
                    repository.setBatteryThreshold(batteryThreshold -1);
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(batteryThreshold < 100 && batteryThreshold >= 1)
                    repository.setBatteryThreshold(batteryThreshold +1);
            }
        });


        repository.getMainServiceOnOff().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean == null)
                    return;
                if(aBoolean) {
                    if(!mainServiceSwitch.isChecked()) mainServiceSwitch.setChecked(true);
                    if(automaticModeLayout.getVisibility() == View.GONE) automaticModeLayout.setVisibility(View.VISIBLE);
                    if(automaticModeLabel.getVisibility() == View.GONE) automaticModeLabel.setVisibility(View.VISIBLE);
                    if(automationSwitch.getVisibility() == View.GONE) automationSwitch.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(new Intent(MainActivity.this, ForegroundService.class));
                    }
                    else
                        startService(new Intent(MainActivity.this, ForegroundService.class));
                }
                else {
                    if(mainServiceSwitch.isChecked()) mainServiceSwitch.setChecked(false);
                    if(automaticModeLayout.getVisibility() == View.VISIBLE) automaticModeLayout.setVisibility(View.GONE);
                    if(automaticModeLabel.getVisibility() == View.VISIBLE) automaticModeLabel.setVisibility(View.GONE);
                    if(automationSwitch.getVisibility() == View.VISIBLE) automationSwitch.setVisibility(View.GONE);
                    stopService(new Intent(MainActivity.this, ForegroundService.class));
                }
            }
        });


        repository.getAutomaticMode().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean == null)
                    return;
                automationSwitch.setChecked(aBoolean);
            }
        });

        repository.getAlertOnUsbCharging().observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean == null)
                    return;
                alertForUsbSwitch.setChecked(aBoolean);
            }
        });

        repository.getBatteryThreshold().observeForever(new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer == null)
                    return;
                batteryThreshold = integer;
                batteryThresholdText.setText(String.valueOf(integer));
            }
        });

        mainServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                repository.setMainServiceOnOff(b);
            }
        });

        automationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                repository.setAutomaticMode(b);
            }
        });

        alertForUsbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                repository.setAlertOnUsbCharging(b);
            }
        });

        initTimer();

    }

    void initTimer(){

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                Intent batteryStatus = getApplication()
                        .registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                float batteryPct = level * 100 / (float)scale;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        batteryPercentageText.setText(String.valueOf(batteryPct) + "%");
                    }
                });

                if(batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        == BatteryManager.BATTERY_STATUS_CHARGING){

                    if(batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                            == BatteryManager.BATTERY_PLUGGED_AC)
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                batteryStatusText.setText("Charging via AC power");
                            }
                        });


                    else if(batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                            == BatteryManager.BATTERY_PLUGGED_USB)

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                batteryStatusText.setText("Charging via USB");
                            }
                        });

                }
                else
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            batteryStatusText.setText("Not Charging");
                        }
                    });

            }
        }, 0, 1000);
    }

    @Override
    protected void onPause() {
        timer.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initTimer();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initTimer();
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}