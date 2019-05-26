package com.example.cache.request;

import android.text.TextUtils;
import com.example.cache.cache.Cache;
import com.example.cache.source.Source;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class GetRequest extends AbstractRequest {

    private static final int MIN = 1024*50;

    public GetRequest(Socket socket, Cache cache, Source source, ExecutorService executor, boolean isFreshSource, long offset) {
        super(socket, cache, source, executor, isFreshSource, offset);
    }


    public Cache getCache(){
        return cache;
    }

    public void setOffset(long offset){
        this.offset = offset;
    }

    public void setSocket(Socket socket){
        this.socket = socket;
    }

    @Override
    void begin() throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        outputStream.write(header.getBytes("UTF-8"));
        if (isSourceRun || sourceCompleted){
            cacheRead(outputStream);
            return;
        }
        if (contentLength - available <MIN){
            sourceCache();
            cacheRead(outputStream);
            return;
        }
        Future<Boolean> future = executor.submit(new SourceCache());
        cacheRead(outputStream);
        try {
            future.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void sourceCache() throws IOException {
        if (TextUtils.isEmpty(header)){
            header = parse();
        }
        super.sourceCache();
    }

    @Override
    public String type() {
        return "Get";
    }

    private class SourceCache implements Callable<Boolean> {

        @Override
        public Boolean call() {
            try {
                sourceCache();
                return true;
            } catch (IOException e) {
                return false;
            }finally {
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void clear() {

    }
}

