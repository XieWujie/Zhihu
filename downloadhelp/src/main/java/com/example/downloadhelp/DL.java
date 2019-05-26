package com.example.downloadhelp;

import android.content.Context;
import android.os.Environment;
import com.example.downloadhelp.cache.Fetch;
import com.example.downloadhelp.cache.Save;
import com.example.downloadhelp.request.DLRequestBuilder;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

public class DL {

    private static volatile DL dl = null;
    private int maxConcurrentCount = 3;
    private static DLBuilder dlBuilder = null;
    private String defaultPath;
    private static DLManager dlManager;
    private Save save;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private Fetch fetch;

    public Fetch getFetch() {
        return fetch;
    }

    public void setFetch(Fetch fetch) {
        this.fetch = fetch;
    }

    public static DLManager with(Context context){
        if (dl == null) {
            if (dlBuilder == null) {
                dlBuilder = getDefaultDLBuilder(context);
            }
            getInstance(dlBuilder);
        }
        return dlManager;
    }

    private static void getInstance(DLBuilder dlBuilder) {
       if (dl == null){
           synchronized (DL.class){
               if (dl == null){
                   dl = new DL(dlBuilder);
               }
           }
       }
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public Save getSave() {
        return save;
    }

    public void setSave(Save save) {
        this.save = save;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    private DL(DLBuilder dlBuilder) {
        if (dlBuilder.getMaxConcurrentCount() > 0){
            this.maxConcurrentCount = dlBuilder.getMaxConcurrentCount();
        }
        if (dlBuilder.getDefaultPath() != null){
            this.defaultPath = dlBuilder.getDefaultPath();
        }
        if (dlManager == null){
            dlManager = new DLManager(this);
        }

    }

    public static void init(String defaultPath){
        DLBuilder builder = new DLBuilder();
        builder.setDefaultPath(defaultPath);
        getInstance(dlBuilder);
    }

    public static DLRequestBuilder<File> load(String url){
        if (dlManager == null || dl == null){
            getInstance (dlBuilder == null ?getDefaultDLBuilder(null):dlBuilder);
        }
        return dlManager.load(url);
    }

    private static DLBuilder getDefaultDLBuilder(Context context){
       DLBuilder builder =  new DLBuilder();
       String url;
       if (isExternalCanUser()){
           url = Environment.getExternalStorageDirectory()+"/DD/";
       }else {
           if (context != null) {
               url = context.getExternalCacheDir().getAbsolutePath() + "/DD/";
           }else {
               throw new RuntimeException("need provide context");
           }
       }
       builder.setDefaultPath(url);
       return builder;
    }


    private static boolean isExternalCanUser(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
