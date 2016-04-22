package com.example.zachlister.tedtabletapp;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

/**
 * Created by zachlister on 4/10/16.
 */
public class TEDTablet extends Application {
    private BluetoothSocket socket;
    private String lang;

    public synchronized void setSocket(BluetoothSocket s){
        socket = s;
    }

    public synchronized BluetoothSocket getSocket(){
        return socket;
    }

    public synchronized void setLang(String l) {
        lang = l;
    }

    public synchronized String getLang(){
        return lang;
    }


}
