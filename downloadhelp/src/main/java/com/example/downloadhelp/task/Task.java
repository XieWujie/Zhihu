package com.example.downloadhelp.task;

import com.example.downloadhelp.listener.DLCompleteListener;
import com.example.downloadhelp.listener.DLFailListener;
import com.example.downloadhelp.listener.DLProgressListener;
import com.example.downloadhelp.listener.StateListener;

import java.io.File;
import java.util.concurrent.Callable;

public interface Task extends Callable<File> {

    void pause();

    void cancel();

    void start();

    void registerStateListener(StateListener listener);

    void registerProgressListener(DLProgressListener listener);

    void freshProgress();

    void recycler();
}
