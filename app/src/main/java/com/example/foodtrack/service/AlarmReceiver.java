package com.example.foodtrack.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new NotificationHelper(context).send("Ne felejtsd el rögzíteni a mai étkezéseidet!");
    }
}
