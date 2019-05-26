package com.example.downloadhelp.converter;

import com.example.downloadhelp.listener.DLCompleteListener;
import com.example.downloadhelp.listener.DLFailListener;
import com.example.downloadhelp.listener.StateListener;
import com.example.downloadhelp.request.RequestOptions;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Target;

public interface WriteEngine {

    File convert(InputStream inputStream, RequestOptions options,long start);

    void pause();

    void stop();

    void start();

    void registerCompleteListener(DLCompleteListener<File> completeListener);

    void registerFailListener(DLFailListener failListener);

    void registerStateListener(StateListener listener);
}
