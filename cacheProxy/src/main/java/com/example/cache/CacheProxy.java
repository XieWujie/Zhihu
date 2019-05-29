package com.example.cache;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.cache.cache.Cache;
import com.example.cache.fileStrategy.FileStrategy;
import com.example.cache.fileStrategy.SimpleFileStrategy;
import com.example.cache.lifecycle.LifecycleListener;
import com.example.cache.lifecycle.LifecycleScopeProvide;
import com.example.cache.request.*;
import com.example.cache.source.GetSourceFactory;
import com.example.cache.source.SourceCallback;
import com.example.cache.source.SourceFactory;
import com.example.cache.util.CacheUtil;
import com.example.cache.util.LOG;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

public class CacheProxy implements LifecycleListener {

    private int port;
    private ExecutorService cacheExecutor;
    private ExecutorService sourceExecutor;
    private Set<RequestFactory> requestFactories = new HashSet<>();
    private Set<SourceFactory> sourceFactories = new HashSet<>();
    private static final String PROXY_HOST = "127.0.0.1";
    private FileStrategy fileStrategy;
    private Cache cache;
    private ServerSocket serverSocket;
    private int threadSize = 3;
    private static CacheProxy cacheProxy;
    private int timeout = 10*1000;
    private LifecycleScopeProvide lifecycleScopeProvide;
    private ConcurrentHashMap<String, Request> requests = new ConcurrentHashMap<>();
    private RequestLooper requestLooper;
    private boolean isFreshSource;
    private Semaphore semaphore;
    private Context context;
    private SharedPreferences preferences;

    public CacheProxy(Context context){
        cacheProxy = this;
        this.context = context;
        initial();
        start();
    }


    public CacheProxy(Context context, boolean lazy){
        this.context = context;
        cacheProxy = this;
        if (!lazy){
            initial();
        }
    }

    public void onlyFromFile(RequestConfig config, SourceCallback callback){
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
           throw new RuntimeException("request too fast");
        }
        if (requestLooper == null){
            throw new RuntimeException("you must start proxy first");
        }
        if (requests.containsKey(config.getUrl())){
            LOG.debug("return");
            return;
        }
        Request request = requestLooper.fromFactory(config,null,callback);
        sourceExecutor.execute(request);
    }

    public void registerLifecycleProvide(LifecycleScopeProvide provide){
        this.lifecycleScopeProvide = provide;
        provide.addObserver(this);
    }

    private void initial(){
        requestFactories.add(new GetRequestFactory());
        if (semaphore == null){
            semaphore = new Semaphore(threadSize*3);
        }
        sourceFactories.add(new GetSourceFactory());
        if (this.cacheExecutor == null){
            cacheExecutor = new ThreadPoolExecutor(0,threadSize,10,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
        }
        if (sourceExecutor == null){
            sourceExecutor = Executors.newFixedThreadPool(threadSize);
        }
        if (lifecycleScopeProvide != null){
            lifecycleScopeProvide.addObserver(this);
        }
        if (fileStrategy == null){
            fileStrategy = new SimpleFileStrategy(context,"cache_proxy");
        }
        if (preferences == null){
            preferences = context.getSharedPreferences("proxy",Context.MODE_PRIVATE);
        }
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public void setFileStrategy(FileStrategy fileStrategy) {
        this.fileStrategy = fileStrategy;
    }

    public static CacheProxy from(Context context){
        if (cacheProxy.context == context){
            return cacheProxy;
        }else {
            return new CacheProxy(context);
        }
    }

    public void start(){
        try {
            serverSocket = new ServerSocket(0,10, InetAddress.getByName(PROXY_HOST));
            port = serverSocket.getLocalPort();
            CountDownLatch latch = new CountDownLatch(1);
            requestLooper = new RequestLooper(serverSocket,this,latch,sourceExecutor,requests);
            new Thread(requestLooper).start();
            latch.await();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setLifecycleScopeProvide(LifecycleScopeProvide lifecycleScopeProvide) {
        this.lifecycleScopeProvide = lifecycleScopeProvide;
    }

    public boolean isFreshSource() {
        return isFreshSource;
    }

    public void setFreshSource(boolean freshSource) {
        isFreshSource = freshSource;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getThreadSize() {
        return threadSize;
    }

    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void clear() {
        LOG.debug("onClear");
    }

    public String getProxyUrl(String originUrl){
        return proxyUrl(originUrl);
    }


    private String proxyUrl(String url){
        return String.format(Locale.US, "http://%s:%d/%s", PROXY_HOST, port, CacheUtil.encode(url) );
    }


    public int getTimeout() {
        return timeout;
    }

    public Set<RequestFactory> getRequestFactories() {
        return requestFactories;
    }

    public Set<SourceFactory> getSourceFactories() {
        return sourceFactories;
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

    public LifecycleScopeProvide getLifecycleScopeProvide() {
        return lifecycleScopeProvide;
    }



    class Builder{

        private CacheProxy proxy;


        public Builder(Context context) {
            proxy = new CacheProxy(context,true);
        }

        public Builder requestFactory(RequestFactory factory){
            proxy.requestFactories.add(factory);
            return this;
        }

        public Builder sourceFactory(SourceFactory factory){
            proxy.sourceFactories.add(factory);
            return this;
        }

        public Builder threadSize(int size){
            proxy.setThreadSize(size);
            return this;
        }

        public Builder lifecycleScopeProvide(LifecycleScopeProvide provide){
            proxy.setLifecycleScopeProvide(provide);
            return this;
        }

        public Builder setTimeout(int timeout){
            proxy.setTimeout(timeout);
            return this;
        }

        public CacheProxy build(){
            proxy.initial();
            proxy.start();
            return proxy;
        }
    }

}
