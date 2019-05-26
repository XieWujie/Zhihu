package com.example.downloadhelp.task;

import com.example.downloadhelp.listener.DLProgressListener;
import com.example.downloadhelp.listener.StateListener;
import com.example.downloadhelp.request.RequestOptions;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class FileTask implements Task {

    private Task task;

    public FileTask(RequestOptions options,ExecutorService executor) {
        if (options.getThreadMode() == RequestOptions.MULTI_THREAD){
            task = new MultiThreadTask(options,executor);
        }else{
            task = new SingleThreadTask(options);
        }
    }

    @Override
    public void pause() {
        task.pause();
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public void start() {
        task.start();
    }

    @Override
    public void registerStateListener(StateListener listener) {
        task.registerStateListener(listener);
    }

    @Override
    public void registerProgressListener(DLProgressListener listener) {
        task.registerProgressListener(listener);
    }

    @Override
    public void freshProgress() {
        task.freshProgress();
    }

    @Override
    public File call() throws Exception {
        return task.call();
    }

    @Override
    public void recycler() {
        task.recycler();
    }
}
