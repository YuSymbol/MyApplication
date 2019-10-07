package com.example.myapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class UpdateTimeService extends Service {
    public UpdateTimeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent timeIntent = new Intent();
                timeIntent.setAction("TIME_CHANGED_ACTION");
                sendBroadcast(timeIntent);
            }
        }, 0, 30*60*1000);
    }
}
