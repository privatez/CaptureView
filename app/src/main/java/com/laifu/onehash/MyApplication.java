package com.laifu.onehash;

import android.app.Application;

import com.onehash.utils.ToastHelper;

/**
 * Created by private on 2017/2/8.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ToastHelper.init(getApplicationContext());
    }
}
