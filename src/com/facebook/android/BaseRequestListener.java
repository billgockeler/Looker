package com.facebook.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import android.util.Log;
import com.facebook.android.AsyncFacebookRunner.RequestListener;

public abstract class BaseRequestListener implements RequestListener {

    public void onFacebookError(FacebookError e, Object state) {
        Log.e("Facebook", e.getMessage());
        e.printStackTrace();
    }

    public void onFileNotFoundException(FileNotFoundException e, Object state) {
        Log.e("Facebook", e.getMessage());
        e.printStackTrace();
    }

    public void onIOException(IOException e, Object state) {
        Log.e("Facebook", e.getMessage());
        e.printStackTrace();
    }

    public void onMalformedURLException(MalformedURLException e, Object state) {
        Log.e("Facebook", e.getMessage());
        e.printStackTrace();
    }

}