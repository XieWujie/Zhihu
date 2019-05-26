package com.example.downloadhelp.request;

import com.example.downloadhelp.Next;
import com.example.downloadhelp.listener.DLCompleteListener;
import com.example.downloadhelp.listener.DLFailListener;
import com.example.downloadhelp.listener.DLProgressListener;
import com.example.downloadhelp.listener.StateListener;

import java.util.concurrent.Future;


public interface Request<T> extends Next<Request<T>>
{

    /*
    *开始request，不等于start
     */
    void begin();

    /*
    *回收request，給request的各种属性赋null
     */
    void recycle();

    void cancel();


    void pause();

    /*
    *start前需要调用begin，和pause对应
     */
    void start();

    boolean isRecycler();

    boolean isRunning();

    boolean isPause();

    boolean isCancel();

    boolean isReady();

    Future<T> get();


    void registerCompleteListener(DLCompleteListener<T> completeListener);

    void registerFailListener(DLFailListener failListener);

    void registerStateListener(StateListener listener);

    void registerProgressListener(DLProgressListener listener);

    void freshProgress();

}
