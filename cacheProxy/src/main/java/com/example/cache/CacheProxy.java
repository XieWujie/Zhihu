package com.example.cache;

import android.content.Context;
import androidx.lifecycle.LifecycleOwner;
import com.example.cache.cache.Cache;
import com.example.cache.fileStrategy.FileStrategy;
import com.example.cache.fileStrategy.SimpleFileStrategy;
import com.example.cache.lifecycle.AndroidLifecycleListener;
import com.example.cache.request.GetRequestFactory;
import com.example.cache.request.Request;
import com.example.cache.request.RequestFactory;
import com.example.cache.request.RequestLooper;
import com.example.cache.source.GetSourceFactory;
import com.example.cache.source.SourceFactory;
import com.example.cache.util.CacheUtil;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CacheProxy implements AndroidLifecycleListener {

    private int port;
    private ExecutorService cacheExecutor;
    private Set<RequestFactory> requestFactories = new HashSet<>();
    private Set<SourceFactory> sourceFactories = new HashSet<>();
    private static final String PROXY_HOST = "127.0.0.1";
    private FileStrategy fileStrategy;
    private Cache cache;
    private Context context;
    private ServerSocket serverSocket;
    private static CacheProxy cacheProxy;
    private ConcurrentHashMap<String, Request> requests = new ConcurrentHashMap<>();

    public CacheProxy(Context context){
        this.context = context;
        cacheExecutor  = Executors.newFixedThreadPool(3);
        this.sourceFactories.add(new GetSourceFactory());
        this.requestFactories.add(new GetRequestFactory());
        this.fileStrategy = new SimpleFileStrategy(context,"proxyCache");
        onCreate();
    }

    public CacheProxy(Builder builder){
        this.context = builder.context;
        this.cacheExecutor = builder.executor;
        this.fileStrategy = builder.fileStrategy;
        this.sourceFactories.add(new GetSourceFactory());
        this.requestFactories.add(new GetRequestFactory());
        onCreate();
    }

    private void onCreate(){
        cacheProxy = this;
        start();
        if (context instanceof LifecycleOwner){
            LifecycleOwner owner = (LifecycleOwner)context;
            owner.getLifecycle().addObserver(this);
        }
    }

    public static CacheProxy from(Context context){
        if (context == cacheProxy.getContext()){
            return cacheProxy;
        }else {
            return new CacheProxy(context);
        }
    }

    public String getProxyUrl(String originUrl){
        return proxyUrl(originUrl);
    }


    public ConcurrentHashMap<String, Request> getRequests() {
        return requests;
    }

    private String proxyUrl(String url){
       return String.format(Locale.US, "http://%s:%d/%s", PROXY_HOST, port, CacheUtil.encode(url) );
    }


    public void start(){
        try {
            serverSocket = new ServerSocket(0,4, InetAddress.getByName(PROXY_HOST));
            port = serverSocket.getLocalPort();
            CountDownLatch latch = new CountDownLatch(1);
            RequestLooper looper = new RequestLooper(serverSocket,this,latch);
            new Thread(looper).start();
            latch.await();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void onStop() {

    }


    public Set<RequestFactory> getRequestFactories() {
        return requestFactories;
    }

    public Set<SourceFactory> getSourceFactories() {
        return sourceFactories;
    }

    public Context getContext() {
        return context;
    }

    public ExecutorService getCacheExecutor() {
        return cacheExecutor;
    }

    public FileStrategy getFileStrategy() {
        return fileStrategy;
    }


    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    class Builder{
        private Context context;
        private ExecutorService executor;
        private FileStrategy fileStrategy;

        public FileStrategy getFileStrategy() {
            return fileStrategy;
        }

        public Builder FileStrategy(FileStrategy fileStrategy) {
            this.fileStrategy = fileStrategy;
            return this;
        }

        public Builder(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }


        public ExecutorService getExecutor() {
            return executor;
        }

        public Builder setExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public CacheProxy build(){
            return new CacheProxy(this);
        }
    }

}
