package com.example.downloadhelp.cache;

import com.example.downloadhelp.listener.OnReadyListener;
import com.example.downloadhelp.request.RequestOptions;

public interface Fetch {

     <T>boolean fetch(RequestOptions options, OnReadyListener<T> listener);

}
