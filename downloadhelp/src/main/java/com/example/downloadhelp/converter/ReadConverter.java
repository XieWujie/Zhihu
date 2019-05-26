package com.example.downloadhelp.converter;


import com.example.downloadhelp.listener.OnReadyListener;
import com.example.downloadhelp.request.RequestOptions;

import java.io.File;

public interface ReadConverter<Target>{

    void convert(RequestOptions options, File file, OnReadyListener<Target> listener);
}
