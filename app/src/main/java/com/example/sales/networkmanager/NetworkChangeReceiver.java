package com.example.sales.networkmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.lang.ref.WeakReference;

class NetworkChangeReceiver extends BroadcastReceiver {
    private WeakReference<NetworkChangeListener> mNetworkChangeListenerWeakReference;

    interface NetworkChangeListener {
        void onNetworkChange(boolean z);
    }

    NetworkChangeReceiver() {
    }

    public void onReceive(Context context, Intent intent) {
        NetworkChangeListener networkChangeListener = (NetworkChangeListener) this.mNetworkChangeListenerWeakReference.get();
        if (networkChangeListener != null) {
            networkChangeListener.onNetworkChange(isNetworkConnected(context));
        }
    }

    /* access modifiers changed from: package-private */
    public void setNetworkChangeListener(NetworkChangeListener networkChangeListener) {
        this.mNetworkChangeListenerWeakReference = new WeakReference<>(networkChangeListener);
    }

    /* access modifiers changed from: package-private */
    public void removeNetworkChangeListener() {
        WeakReference<NetworkChangeListener> weakReference = this.mNetworkChangeListenerWeakReference;
        if (weakReference != null) {
            weakReference.clear();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isNetworkConnected(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return netInfo != null && netInfo.isAvailable() && netInfo.isConnected();
    }
}
