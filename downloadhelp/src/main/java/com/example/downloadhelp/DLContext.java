package com.example.downloadhelp;

import java.util.concurrent.*;

public class DLContext {
    private ExecutorService executor;

    public DLContext() {

    }



    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void builderExecutor(){
        BlockingQueue<Runnable> workerQueue = new LinkedBlockingDeque<>();
        executor = new ThreadPoolExecutor(1,3,5,TimeUnit.SECONDS,workerQueue);
    }
}
