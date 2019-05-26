package com.example.downloadhelp;

import com.example.downloadhelp.cache.Save;

import java.util.concurrent.ExecutorService;

public class DLBuilder {
    private int maxConcurrentCount = 3;
    private String defaultPath;
    private Save save;
    private ExecutorService executor;

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public Save getSave() {
        return save;
    }

    public void setSave(Save save) {
        this.save = save;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    public int getMaxConcurrentCount() {
        return maxConcurrentCount;
    }


    public void setMaxConcurrentCount(int maxConcurrentCount) {
        this.maxConcurrentCount = maxConcurrentCount;
    }
}