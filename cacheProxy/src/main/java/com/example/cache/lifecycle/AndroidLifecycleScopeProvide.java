package com.example.cache.lifecycle;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.example.cache.lifecycle.LifecycleListener;
import com.example.cache.lifecycle.LifecycleScopeProvide;

import java.util.concurrent.LinkedBlockingQueue;

public class AndroidLifecycleScopeProvide implements LifecycleObserver, LifecycleScopeProvide {

    private LinkedBlockingQueue<LifecycleListener> listeners = new LinkedBlockingQueue<>();

    @Override
    public void addObserver(LifecycleListener listener) {
        try {
            listeners.put(listener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void clear(){
        while (!listeners.isEmpty()){
            LifecycleListener listener = listeners.remove();
            listener.clear();
        }
    }
}
