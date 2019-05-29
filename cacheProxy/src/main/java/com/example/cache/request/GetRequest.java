package com.example.cache.request;

import android.content.SharedPreferences;
import com.example.cache.cache.Cache;
import com.example.cache.source.Source;
import com.example.cache.util.Pools;

import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class GetRequest extends AbstractRequest {

    private static final int MIN = 1024*50;
    private static Pools.Pool<GetRequest> pool = new Pools.SimplePool<GetRequest>(10);


    public GetRequest(Socket socket, Cache cache, Source source, ExecutorService executor, Semaphore semaphore, SharedPreferences preferences,boolean isFreshSource, long offset) {
        super(socket, cache, source, executor,semaphore,preferences,isFreshSource, offset);
    }

    public void init(Socket socket, Cache cache, Source source, ExecutorService executor,Semaphore semaphore, SharedPreferences preferences,boolean isFreshSource, long offset){
        this.socket = socket;
        this.cache = cache;
        this.source = source;
        this.executor = executor;
        this.isFreshSource = isFreshSource;
        this.offset = offset;
        this.semaphore = semaphore;
        this.preferences = preferences;
        this.editor = preferences.edit();
    }
    public Cache getCache(){
        return cache;
    }

    @Override
    void begin(){
        if (onlySource){
            sourceCache();
            return;
        }
        if (isSourceRun){
            cacheRead();
            return;
        }
        if (sourceCompleted){
            cache.isSourceFinish(true);
            cacheRead();
            return;
        }
        executor.submit(new SourceCache());
        cacheRead();
    }


    public static GetRequest acquire(){
       return pool.acquire();
    }

    @Override
    public void release() {
        super.release();
        pool.release(this);
    }

    @Override
    public String type() {
        return "Get";
    }

    private class SourceCache implements Callable<Boolean> {

        @Override
        public Boolean call() {
            sourceCache();
            return true;
        }
    }
}

