package com.example.cache.lifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public interface AndroidLifecycleListener extends LifecycleListener , LifecycleObserver {

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void clear();

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    @Override
    void onStop();
}
