package com.example.sales.networkmanager;

import android.os.AsyncTask;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class CheckInternetTask extends AsyncTask<Void, Void, Boolean> {
    private WeakReference<TaskFinished<Boolean>> mCallbackWeakReference;

    CheckInternetTask(TaskFinished<Boolean> callback) {
        this.mCallbackWeakReference = new WeakReference<>(callback);
    }

    /* access modifiers changed from: protected */
    public Boolean doInBackground(Void... params) {
        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) new URL("https://clients3.google.com/generate_204").openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            urlConnection.setRequestProperty("User-Agent", "Android");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setConnectTimeout(1500);
            urlConnection.connect();
            return Boolean.valueOf(urlConnection.getResponseCode() == 204 && urlConnection.getContentLength() == 0);
        } catch (IOException e2) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Boolean isInternetAvailable) {
        TaskFinished<Boolean> callback = (TaskFinished) this.mCallbackWeakReference.get();
        if (callback != null) {
            callback.onTaskFinished(isInternetAvailable);
        }
    }
}
