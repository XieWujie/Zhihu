package com.example.cache.request;

import java.io.File;

enum State{
    SourceReady,Finish,Release,Running,Error;
    private File source;
    private Exception e;

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public Exception getE() {
        return e;
    }

    public void setE(Exception e) {
        this.e = e;
    }
}