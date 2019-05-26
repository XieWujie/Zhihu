package com.example.downloadhelp.cache;

import android.graphics.Bitmap;
import com.example.downloadhelp.listener.OnReadyListener;

import java.io.File;
import java.io.InputStream;

public class SaveSupport implements Save {

    @Override
    public <Target> Target save(String key, File file, OnReadyListener<Target> listener) {
        return null;
    }
}
