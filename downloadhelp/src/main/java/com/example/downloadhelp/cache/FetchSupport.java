package com.example.downloadhelp.cache;

import com.example.downloadhelp.listener.OnReadyListener;
import com.example.downloadhelp.request.RequestOptions;

public class FetchSupport implements Fetch {

    @Override
    public <T> boolean fetch(RequestOptions options, OnReadyListener<T> listener) {
        return false;
    }
}
