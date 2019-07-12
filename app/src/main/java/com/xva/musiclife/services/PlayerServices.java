package com.xva.musiclife.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PlayerServices extends Service {

    private PlayerServicesBinder binder = new PlayerServicesBinder();

    public PlayerServices() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
