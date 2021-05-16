/*
 * Created by Praveen Kumar for BatteryAlert.
 * Copyright (c) 2021.
 * Last modified on 17/5/21 12:05 AM.
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
import android.os.BatteryManager;

public class ChargingStateChangeReceiver extends BroadcastReceiver {

    Repository repository;

    volatile boolean mainServiceOnOff;
    volatile boolean automaticMode;

    @Override
    public void onReceive(Context context, Intent intent) {


        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        repository = Repository.getInstance(context);

        mainServiceOnOff = repository.getMainServiceOnOff().getValue();
        automaticMode = repository.getAutomaticMode().getValue();

        if(!isCharging){
            context.stopService(new Intent(context, ForegroundService.class));
        }

        if(mainServiceOnOff && automaticMode){
            context.startService(new Intent(context, ForegroundService.class));
        }

    }
}
