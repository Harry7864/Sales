package com.example.sales;

import android.app.Application;
import com.example.sales.networkmanager.DroidNet;

public class MyApp extends Application {
    public void onCreate() {
        super.onCreate();
        DroidNet.init(this);
    }

    public void onLowMemory() {
        super.onLowMemory();
        DroidNet.getInstance().removeAllInternetConnectivityChangeListeners();
    }
}
