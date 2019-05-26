package com.example.downloadhelp.request;

import com.example.downloadhelp.listener.DLProgressListener;

import java.util.ArrayList;
import java.util.List;

public class  ObservableRequest<ResourceType> extends SimpleRequest<ResourceType> implements DLProgressListener {

    private List<DLProgressListener> progressListeners = new ArrayList<>();


    @Override
    protected void beforeBegin() {
        super.beforeBegin();
        task.registerProgressListener(this);
    }

    @Override
    public void onProgress(int loaded, int totalLength) {
        for (int i = progressListeners.size()-1;i>-1;i--){
            progressListeners.get(i).onProgress(loaded,totalLength);
        }
    }


    @Override
    public void freshProgress() {
        task.freshProgress();
    }

    @Override
    public void registerProgressListener(DLProgressListener listener) {
        if (listener != null){
            progressListeners.add(listener);
        }
    }


}
