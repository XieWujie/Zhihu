package com.example.cache.request;

import com.example.cache.Release;
import com.example.cache.lifecycle.LifecycleListener;

public interface Request extends Runnable , LifecycleListener, Release {


    String type();

    String realUrl();

    void registerListener(RequestListener listener);
}


