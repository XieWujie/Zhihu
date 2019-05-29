package com.example.cache.request;

import android.text.TextUtils;
import com.example.cache.CacheProxy;
import com.example.cache.lifecycle.LifecycleScopeProvide;
import com.example.cache.source.SourceCallback;
import com.example.cache.util.CacheUtil;
import com.example.cache.util.LOG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestLooper implements Runnable {

    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-");
    private ServerSocket serverSocket;
    private CacheProxy proxy;
    private CountDownLatch latch;
    private ExecutorService sourceExecutor;
    private LifecycleScopeProvide provide;
    private ConcurrentHashMap<String,Request> requests;
    private Semaphore semaphore;

    public RequestLooper(ServerSocket serverSocket, CacheProxy proxy, CountDownLatch latch,ExecutorService sourceExecutor,ConcurrentHashMap<String,Request> requests) {
        this.latch = latch;
        this.serverSocket = serverSocket;
        this.proxy = proxy;
        this.provide = proxy.getLifecycleScopeProvide();
        this.sourceExecutor = sourceExecutor;
        this.requests = requests;
        this.semaphore = proxy.getSemaphore();
    }

    @Override
    public void run() {
        try {
            latch.countDown();
            loop();
            serverSocket.close();
        } catch (Throwable throwable) {
            LOG.debug(throwable);
            throw new RuntimeException(throwable);
        }
    }

    private void loop()throws Exception{
        Socket socket = null;
        RequestConfig config = null;
        while (!Thread.currentThread().isInterrupted()){
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException("request too fast");
            }
            socket = findSocket();
            if (socket == null){
                continue;
            }
            config = findRequestConfig(socket.getInputStream());
            Request request = fromFactory(config,socket);
            if (request == null){
                continue;
            }
            if (provide != null){
                provide.addObserver(request);
            }
            sourceExecutor.execute(request);
        }
    }

    private Socket findSocket(){
        Socket socket = null;
        try {
            socket = serverSocket.accept();
            socket.setSoTimeout(proxy.getTimeout());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return socket;
    }


    public Request fromFactory(RequestConfig config,Socket socket){
        Set<RequestFactory> factories = proxy.getRequestFactories();
        Request request = null;
        for(RequestFactory factory:factories){
            request = factory.from(config,socket,proxy);
            if (request != null){
                requests.put(request.realUrl(),request);
                return request;
            }
        }
        throw new RuntimeException("can not get the request:"+socket);
    }

    public Request fromFactory(final RequestConfig config, Socket socket, final SourceCallback callback){
        final Request request = fromFactory(config,socket);
        request.registerListener(new RequestListener() {
            @Override
            public void callback(State state) {
                switch (state){
                    case SourceReady:{
                        callback.callback(null,state.getSource());
                        break;
                    }
                    case Error:{
                        callback.callback(state.getE(),null);
                        break;
                    }
                    case Release:{
                        requests.remove(config.getUrl());
                        break;
                    }
                    default:
                        break;
                }
            }
        });
        return request;
    }
    public  long findRangeOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            return Long.parseLong(rangeValue);
        }
        return 0;
    }

    private RequestConfig findRequestConfig(InputStream inputStream){
        String u = parseInputStream(inputStream);
        RequestConfig config = new RequestConfig();
        int last = u.indexOf("\n");
        String firstLine = u.substring(0,last==-1?u.length():last);
        String[] a = firstLine.split(" ");
        if (a.length == 3){
            config.setMethod(a[0]);
            String url;
            if (a[1].indexOf("/") == 0){
                url = a[1].substring(1);
            }else {
                url = a[1];
            }
            config.setUrl(CacheUtil.decode(url));
        }else {
            throw new RuntimeException("can not find config from inputStream:"+u+a.toString());
        }
        config.setOffset(findRangeOffset(u));
        return config;
    }

    private String parseInputStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder(30);
        String line;
        try {
            while (!TextUtils.isEmpty((line = reader.readLine()))){
                builder.append(line).append("\n");
            }
        }catch (IOException i){
            i.printStackTrace();
        }
        return builder.toString();
    }
}
