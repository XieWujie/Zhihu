package com.example.cache.util;

import android.text.TextUtils;
import android.util.Log;

public class LOG {
    public static final String TAG = "cacheProxy";

    public static void debug(String message){
        if (TextUtils.isEmpty(message)){
            return;
        }
        Log.d(TAG,message);
    }

    public static void debug(Throwable throwable){
        if (throwable == null){
            return;
        }
        Log.d(TAG,throwable.getMessage());
    }
}
