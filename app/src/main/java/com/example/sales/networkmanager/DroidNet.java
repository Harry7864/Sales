package com.example.sales.networkmanager;

import android.content.Context;
import android.content.IntentFilter;
import com.example.sales.networkmanager.NetworkChangeReceiver;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class DroidNet implements NetworkChangeReceiver.NetworkChangeListener {
    private static final String CONNECTIVITY_CHANGE_INTENT_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final Object LOCK = new Object();
    private static volatile DroidNet sInstance;
    /* access modifiers changed from: private */
    public TaskFinished<Boolean> mCheckConnectivityCallback;
    private WeakReference<Context> mContextWeakReference;
    private List<WeakReference<DroidListener>> mInternetConnectivityListenersWeakReferences;
    private boolean mIsInternetConnected = false;
    private boolean mIsNetworkChangeRegistered = false;
    private NetworkChangeReceiver mNetworkChangeReceiver;

    private DroidNet(Context context) {
        this.mContextWeakReference = new WeakReference<>(context.getApplicationContext());
        this.mInternetConnectivityListenersWeakReferences = new ArrayList();
    }

    public static DroidNet init(Context context) {
        if (context != null) {
            if (sInstance == null) {
                synchronized (LOCK) {
                    if (sInstance == null) {
                        sInstance = new DroidNet(context);
                    }
                }
            }
            return sInstance;
        }
        throw new NullPointerException("context can not be null");
    }

    public static DroidNet getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        throw new IllegalStateException("call init(Context) in your application class before calling getInstance()");
    }

    public void addInternetConnectivityListener(DroidListener droidListener) {
        if (droidListener != null) {
            this.mInternetConnectivityListenersWeakReferences.add(new WeakReference(droidListener));
            if (this.mInternetConnectivityListenersWeakReferences.size() == 1) {
                registerNetworkChangeReceiver();
            } else {
                publishInternetAvailabilityStatus(this.mIsInternetConnected);
            }
        }
    }

    public void removeInternetConnectivityChangeListener(DroidListener droidListener) {
        List<WeakReference<DroidListener>> list;
        if (droidListener != null && (list = this.mInternetConnectivityListenersWeakReferences) != null) {
            Iterator<WeakReference<DroidListener>> iterator = list.iterator();
            while (true) {
                if (!iterator.hasNext()) {
                    break;
                }
                WeakReference<DroidListener> reference = iterator.next();
                if (reference == null) {
                    iterator.remove();
                } else {
                    DroidListener listener = (DroidListener) reference.get();
                    if (listener == null) {
                        reference.clear();
                        iterator.remove();
                    } else if (listener == droidListener) {
                        reference.clear();
                        iterator.remove();
                        break;
                    }
                }
            }
            if (this.mInternetConnectivityListenersWeakReferences.size() == 0) {
                unregisterNetworkChangeReceiver();
            }
        }
    }

    public void removeAllInternetConnectivityChangeListeners() {
        List<WeakReference<DroidListener>> list = this.mInternetConnectivityListenersWeakReferences;
        if (list != null) {
            Iterator<WeakReference<DroidListener>> iterator = list.iterator();
            while (iterator.hasNext()) {
                WeakReference<DroidListener> reference = iterator.next();
                if (reference != null) {
                    reference.clear();
                }
                iterator.remove();
            }
            unregisterNetworkChangeReceiver();
        }
    }

    private void registerNetworkChangeReceiver() {
        Context context = (Context) this.mContextWeakReference.get();
        if (context != null && !this.mIsNetworkChangeRegistered) {
            NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
            this.mNetworkChangeReceiver = networkChangeReceiver;
            networkChangeReceiver.setNetworkChangeListener(this);
            context.registerReceiver(this.mNetworkChangeReceiver, new IntentFilter(CONNECTIVITY_CHANGE_INTENT_ACTION));
            this.mIsNetworkChangeRegistered = true;
        }
    }

    private void unregisterNetworkChangeReceiver() {
        NetworkChangeReceiver networkChangeReceiver;
        Context context = (Context) this.mContextWeakReference.get();
        if (!(context == null || (networkChangeReceiver = this.mNetworkChangeReceiver) == null || !this.mIsNetworkChangeRegistered)) {
            try {
                context.unregisterReceiver(networkChangeReceiver);
            } catch (IllegalArgumentException e) {
            }
            this.mNetworkChangeReceiver.removeNetworkChangeListener();
        }
        this.mNetworkChangeReceiver = null;
        this.mIsNetworkChangeRegistered = false;
        this.mCheckConnectivityCallback = null;
    }

    public void onNetworkChange(boolean isNetworkAvailable) {
        if (isNetworkAvailable) {
            this.mCheckConnectivityCallback = new TaskFinished<Boolean>() {
                public void onTaskFinished(Boolean isInternetAvailable) {
                    TaskFinished unused = DroidNet.this.mCheckConnectivityCallback = null;
                    DroidNet.this.publishInternetAvailabilityStatus(isInternetAvailable.booleanValue());
                }
            };
            new CheckInternetTask(this.mCheckConnectivityCallback).execute(new Void[0]);
            return;
        }
        publishInternetAvailabilityStatus(false);
    }

    /* access modifiers changed from: private */
    public void publishInternetAvailabilityStatus(boolean isInternetAvailable) {
        this.mIsInternetConnected = isInternetAvailable;
        List<WeakReference<DroidListener>> list = this.mInternetConnectivityListenersWeakReferences;
        if (list != null) {
            Iterator<WeakReference<DroidListener>> iterator = list.iterator();
            while (iterator.hasNext()) {
                WeakReference<DroidListener> reference = iterator.next();
                if (reference == null) {
                    iterator.remove();
                } else {
                    DroidListener listener = (DroidListener) reference.get();
                    if (listener == null) {
                        iterator.remove();
                    } else {
                        listener.onInternetConnectivityChanged(isInternetAvailable);
                    }
                }
            }
            if (this.mInternetConnectivityListenersWeakReferences.size() == 0) {
                unregisterNetworkChangeReceiver();
            }
        }
    }
}
