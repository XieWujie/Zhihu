package com.example.downloadhelp.cache;

import com.example.downloadhelp.listener.OnReadyListener;

import java.io.File;
import java.io.InputStream;

public interface Save{

    <Target>Target save(String key, File file, OnReadyListener<Target> listener);

}
