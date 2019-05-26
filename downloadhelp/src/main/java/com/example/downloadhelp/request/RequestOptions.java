package com.example.downloadhelp.request;

import org.jetbrains.annotations.NotNull;

public class RequestOptions<T extends RequestOptions,R> implements Cloneable {

    protected String url;
    protected String parentPath;
    protected Class<R> resourceType;
    protected String fileName;
    protected int threadMode = SINGLE_THREAD;
    protected int segment = 2;
    protected long scheduleTime = 1000;

    public int getSegment() {
        return segment;
    }

    public T segment(int segment){
        this.segment = segment;
        return (T)this;
    }

    public T scheduleTime(long scheduleTime){
      this.scheduleTime = scheduleTime;
      return (T)this;
    }

    public static int SINGLE_THREAD = 0;
    public static int MULTI_THREAD = 1;

    public int getThreadMode() {
        return threadMode;
    }

    public T threadMode(int threadMode) {
        this.threadMode = threadMode;
        return (T)this;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }


    public String getParentPath() {
        return parentPath;
    }

    public T path(String filePath) {
        this.parentPath = filePath;
        return (T)this;
    }

    public RequestOptions(){

    }


    public T url(@NotNull String url){
        this.url = url;
        return (T)this;
    }

    @Override
    protected Object clone(){
        try {
            RequestOptions requestOptions = (RequestOptions<?,?>)super.clone();
            return requestOptions;
        }catch (CloneNotSupportedException e){
            throw new RuntimeException(e);
        }
    }
}
