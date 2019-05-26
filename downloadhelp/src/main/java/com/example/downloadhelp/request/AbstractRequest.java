package com.example.downloadhelp.request;

import com.example.downloadhelp.listener.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

abstract class AbstractRequest<ResourceType> implements Request<ResourceType>,DLFailListener,DLCompleteListener<File>,StateListener {

    protected List<DLCompleteListener<ResourceType>> dlCompleteListeners = new ArrayList<>();
    protected List<DLFailListener> failListeners = new ArrayList<>();
    protected List<StateListener> stateListeners = new ArrayList<>();
    protected boolean isRunning = false;
    private boolean isCancel = false;
    private boolean isPause = false;
    private Request nextRequest;

    @Override
    public  void setNext(Request<ResourceType> request) {
        this.nextRequest = request;
    }

    @Override
    public Request getNext() {
        return nextRequest;
    }

    @Override
    public void registerCompleteListener(DLCompleteListener completeListener) {
        if (completeListener != null){
            dlCompleteListeners.add(completeListener);
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void registerFailListener(DLFailListener failListener) {
        if (failListener != null){
            failListeners.add(failListener);
        }
    }

    @Override
    public void registerStateListener(StateListener listener) {
        if (listener != null){
            stateListeners.add(listener);
        }
    }

    @Override
    public void onFail(Exception e) {
        for (int i = failListeners.size()-1;i>-1;i--){
            failListeners.get(i).onFail(e);
        }
    }


    @Override
    public void onState(String url, State state) {
        switch (state){
            case ERROR:{
                onFail(state.getException());
                isRunning = false;
                break;
            }
            case FINISH:{
                onComplete(url,(File) state.getResult());
                isRunning = false;
                break;
            }
            case CANCEL:{
                isPause = true;
                isRunning = false;
                break;
            }
            case PAUSE:{
                isPause = true;
                isRunning = false;
                break;
            }
            case RUNNING:{
                isPause = false;
                isRunning = true;
            }
        }
        for (int i = stateListeners.size()-1;i>-1;i--){
           stateListeners.get(i).onState(url,state);
        }
    }

    @Override
    public boolean isPause() {
        return isPause;
    }

    @Override
    public boolean isCancel() {
        return isCancel;
    }

    @Override
    public void registerProgressListener(DLProgressListener listener) {

    }

    @Override
    public void freshProgress() {

    }


}
