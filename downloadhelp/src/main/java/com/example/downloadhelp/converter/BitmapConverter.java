package com.example.downloadhelp.converter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.downloadhelp.listener.OnReadyListener;
import com.example.downloadhelp.request.RequestOptions;

import java.io.*;

public class BitmapConverter implements ReadConverter<Bitmap>{
    @Override
    public void convert(RequestOptions options, File file, OnReadyListener<Bitmap> listener) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        listener.onReady(bitmap);
    }
}
