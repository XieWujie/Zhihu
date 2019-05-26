package com.example.downloadhelp.request;

import com.example.downloadhelp.cache.Fetch;
import com.example.downloadhelp.cache.Save;
import com.example.downloadhelp.converter.ReadConverter;
import com.example.downloadhelp.listener.*;
import com.example.downloadhelp.target.Target;
import com.example.downloadhelp.task.Task;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class SimpleRequest<Resource> extends AbstractRequest<Resource> implements OnReadyListener<Resource> {

    protected static Object requestPool = new Object();
    protected SimpleRequest next;
    protected static SimpleRequest sPool;
    protected RequestOptions options;
    protected Task task;
    protected Save save;
    protected Fetch fetch;
    protected ExecutorService runExecutor;
    protected ReadConverter<Resource> readConverter;
    protected Resource result;
    private boolean isReady = false;
    private Future future;
    private boolean isRecycler = false;
    private Target<Resource> target;
    private Executor callbackExecutor;


    public SimpleRequest() {

    }



    public SimpleRequest(RequestOptions options, Task task, Save save, Fetch fetch,
                         ExecutorService runExecutor,Executor callbackExecutor, ReadConverter<Resource> readConverter) {
        init(options,task,save,fetch,runExecutor,callbackExecutor,readConverter,null);
    }

    public SimpleRequest(RequestOptions options, Task task, Save save, Fetch fetch,
                         ExecutorService runExecutor,Executor callbackExecutor, ReadConverter<Resource> readConverter,Target<Resource> target) {
        init(options,task,save,fetch,runExecutor,callbackExecutor,readConverter,target);
    }


    public void init(RequestOptions options, Task task, Save save, Fetch fetch,
                     ExecutorService runExecutor,Executor callbackExecutor, ReadConverter<Resource> readConverter,Target<Resource> target) {
        this.options = options;
        this.task = task;
        this.save = save;
        this.fetch = fetch;
        this.runExecutor = runExecutor;
        this.readConverter = readConverter;
        task.registerStateListener(this);
        this.callbackExecutor = callbackExecutor;
        this.target = target;
    }

    @Override
    public void begin() {
        beforeBegin();
        isRunning = true;
        isReady = true;
        strategy();
    }

    protected void strategy(){
        if (fetch != null) {
            boolean have = fetch.fetch(options, this);
            if (!have) {
                runTask();
            }
        }else {
            runTask();
        }
    }

    @Override
    public boolean isRecycler() {
        return isRecycler;
    }

    private void runTask(){
        future = runExecutor.submit(task);
    }



    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public void pause() {
        task.pause();
    }

    @Override
    public Future<Resource> get() {
        return future;
    }

    @Override
    public void onReady(Resource resource) {
        for (int i = dlCompleteListeners.size()-1;i>-1;i--){
            dlCompleteListeners.get(i).onComplete(options.getUrl(),resource);
        }
        if (task != null){
            callbackExecutor.execute(new ResourceReadyCallback<Resource>(resource,options,target));
        }
    }


    public static<T> SimpleRequest<T> obtain() {
        synchronized (requestPool) {
            if (sPool != null) {
                SimpleRequest<T> request = (SimpleRequest<T>)sPool;
                sPool = request.next;
                request.next = null;
                return request;
            }
        }
        return new SimpleRequest<T>();
    }



    @Override
    public void start() {
        task.start();
    }

    @Override
    public void recycle() {
        synchronized (requestPool) {
            this.task.recycler();
            this.task = null;
            this.save = null;
            this.fetch = null;
            this.options = null;
            this.failListeners.clear();
            this.dlCompleteListeners.clear();
            this.isRecycler = true;
            this.stateListeners.clear();
            if (sPool == null) {
                sPool = this;
                sPool.next = null;
            } else {
                SimpleRequest request = sPool.next;
                sPool = this;
                sPool.next = request;
            }
        }
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    protected void beforeBegin(){

    }


    @Override
    public void onComplete(String url, File file) {
        if (readConverter != null) {
            readConverter.convert(options, file, this);
        }else {
            if (File.class == options.resourceType){
                onReady((Resource) file);
            }
        }
    }
}
