/*
 * Created by Praveen Kumar for BatteryAlert.
 * Copyright (c) 2021.
 * Last modified on 17/5/21 12:03 PM.
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

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class
Repository {

    private static Repository repository;
    Context context;

    MutableLiveData<Integer> batteryPercentage = new MutableLiveData<>();
    MutableLiveData<String> chargeType = new MutableLiveData<>();
    MutableLiveData<Integer> batteryThreshold = new MutableLiveData<>();
    MutableLiveData<Boolean> mainServiceOnOff = new MutableLiveData<>();
    MutableLiveData<Boolean> alertOnUsbCharging = new MutableLiveData<>();
    MutableLiveData<Boolean> automaticMode = new MutableLiveData<>();
    MutableLiveData<Boolean> alarmIsOn = new MutableLiveData<>();

    public Repository(Context context) {
        this.context = context;
        batteryThreshold.setValue(context.getSharedPreferences("settings", 0).getInt("batteryThreshold", 100));
        alertOnUsbCharging.setValue(context.getSharedPreferences("settings", 0).getBoolean("alertOnUsbCharging", false));
        mainServiceOnOff.setValue(context.getSharedPreferences("settings", 0).getBoolean("mainServiceOnOff", false));
        automaticMode.setValue(context.getSharedPreferences("settings", 0).getBoolean("automaticMode", false));
        alarmIsOn.setValue(context.getSharedPreferences("settings", 0).getBoolean("alarmIsOn", false));
    }

    public static Repository getInstance(Context context){
        if(repository == null)
            repository = new Repository(context);

        return repository;
    }


    public LiveData<Integer> getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage.setValue(batteryPercentage);
    }


    public LiveData<String> getChargeType() {
        return chargeType;
    }

    public void setChargeType(String chargeType) {
        this.chargeType.setValue(chargeType);
    }

    public LiveData<Integer> getBatteryThreshold() {
        return batteryThreshold;
    }

    public void setBatteryThreshold(int batteryThreshold) {
        context.getSharedPreferences("settings", 0).edit().putInt("batteryThreshold", batteryThreshold).apply();
        this.batteryThreshold.setValue(batteryThreshold);
    }

    public LiveData<Boolean> getAlertOnUsbCharging() {
        return alertOnUsbCharging;
    }

    public void setAlertOnUsbCharging(boolean alertOnUsbCharging) {
        context.getSharedPreferences("settings", 0).edit().putBoolean("alertOnUsbCharging", alertOnUsbCharging).apply();
        this.alertOnUsbCharging.setValue(alertOnUsbCharging);
    }


    public LiveData<Boolean> getMainServiceOnOff() {
        return mainServiceOnOff;
    }

    public void setMainServiceOnOff(boolean mainServiceOnOff) {
        context.getSharedPreferences("settings", 0).edit().putBoolean("mainServiceOnOff", mainServiceOnOff).apply();
        this.mainServiceOnOff.setValue(mainServiceOnOff);
    }

    public LiveData<Boolean> getAutomaticMode() {
        return automaticMode;
    }

    public void setAutomaticMode(boolean automaticMode) {
        context.getSharedPreferences("settings", 0).edit().putBoolean("automaticMode", automaticMode).apply();
        this.automaticMode.setValue(automaticMode);
    }

}
