package com.example.downloadhelp.listener;

import java.io.File;

public enum State {

    FINISH,PAUSE,RUNNING,CANCEL,ERROR;

   private Object result;
   private Exception exception;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
