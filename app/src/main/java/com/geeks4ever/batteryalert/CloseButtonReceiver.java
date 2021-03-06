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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CloseButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        context.stopService(new Intent(context, ForegroundService.class));

    }

}