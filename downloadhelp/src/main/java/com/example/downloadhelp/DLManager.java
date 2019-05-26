package com.example.downloadhelp;

import android.graphics.Bitmap;
import android.util.Log;
import androidx.lifecycle.LifecycleObserver;
import com.example.downloadhelp.converter.BitmapConverter;
import com.example.downloadhelp.converter.ReadConverter;
import com.example.downloadhelp.listener.State;
import com.example.downloadhelp.listener.StateListener;
import com.example.downloadhelp.request.DLRequestBuilder;
import com.example.downloadhelp.request.Request;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DLManager implements StateListener, LifecycleObserver {
    private DL dl;
    private Map<String,Request> requests = new HashMap<String,Request>();
    private ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
    private Future progressFuture;

    public Request getRequest(String key){
        return requests.get(key);
    }

    private void freshProgress(){
        boolean isPause = true;
        Log.d("res-","fresh"+requests.size());
      for (Request request:requests.values()){
          request.freshProgress();
          Log.d("res-","isRunning"+request.isRunning());
          if (request.isRunning()){
              isPause = false;
          }
    }
      if (isPause){
          stopProgress();
      }
    }

    private Runnable scheduledTask = new Runnable() {
        @Override
        public void run() {
            freshProgress();
        }
    };

    public void addRequest(String key,Request request){
        if (requests.containsKey(key)){
            Request<?> r = requests.get(key);
            addToLink(r,request);
            if (r.isPause()){
                r.start();
            }
        }else {
            requests.put(key,request);
            request.registerStateListener(this);
            request.begin();
        }
    }

    private synchronized void addToLink(@NotNull Request<?> origin,Request target){
        Request<?> next = origin.getNext();
        if (next == null){
            origin.setNext(target);
            return;
        }
        for (; ;){
            if (next.getNext() != null){
                next = next.getNext();
            }else {
                next.setNext(target);
                break;
            }
        }
    }

    private void runFirstRequest(String key){
        Request<?> origin = requests.remove(key);
        if (origin == null){
            return ;
        }
        Request<?> target = origin.getNext();
        origin.recycle();
        if (target != null){
            requests.put(key,target);
            target.registerStateListener(this);
            target.begin();
        }
    }

    public DLRequestBuilder<Bitmap> asBitmap(){
       return asResource(new BitmapConverter(),Bitmap.class);
    }

    public DLManager(DL dl){
        this.dl = dl;
    }

    public void stopProgress(){
        if (progressFuture!= null &&!progressFuture.isCancelled())
        progressFuture.cancel(false);
    }

    public void startProgress(){
        if (progressFuture == null || progressFuture.isCancelled())
        progressFuture = schedule.scheduleAtFixedRate(scheduledTask,0,1000,TimeUnit.MILLISECONDS);
    }

    public DLRequestBuilder<File> load(@NotNull String url){
        return asFile().load(url);
    }

    public DLRequestBuilder<File> asFile(){
       return asResource(null,File.class);
    }


    public<ResourceType> DLRequestBuilder<ResourceType> asResource(ReadConverter<ResourceType> readConverter,Class<ResourceType> resourceTypeClass){
        return new DLRequestBuilder<>(dl,this, readConverter,resourceTypeClass);
    }

    @Override
    public void onState(String url, State state) {
        if (state == State.FINISH || state == State.CANCEL ||state == State.ERROR){
            runFirstRequest(url);
            if (requests.size() == 0){
                stopProgress();
            }
        }
        if (state == State.RUNNING){
            startProgress();
        }
    }
}
