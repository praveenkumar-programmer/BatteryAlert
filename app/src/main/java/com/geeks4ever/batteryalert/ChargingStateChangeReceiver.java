/*
 * Created by Praveen Kumar for BatteryAlert.
 * Copyright (c) 2021.
 * Last modified on 17/5/21 12:43 PM.
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class ChargingStateChangeReceiver extends BroadcastReceiver {

    Repository repository;

    volatile boolean mainServiceOnOff;
    volatile boolean automaticMode;

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isCharging = isConnected(context);

        repository = Repository.getInstance(context);

        mainServiceOnOff = repository.getMainServiceOnOff().getValue();
        automaticMode = repository.getAutomaticMode().getValue();

        if(!isCharging){
            context.stopService(new Intent(context, ForegroundService.class));
            return;
        }


        if(mainServiceOnOff && automaticMode){
            context.startService(new Intent(context, ForegroundService.class));
        }

    }

    public static boolean isConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

}
