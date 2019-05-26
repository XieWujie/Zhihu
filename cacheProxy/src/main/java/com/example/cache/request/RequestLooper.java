package com.example.cache.request;

import com.example.cache.CacheProxy;
import com.example.cache.util.LOG;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestLooper implements Runnable {

    private ServerSocket serverSocket;
    private CacheProxy proxy;
    private CountDownLatch latch;
    private ExecutorService sourceExecutor = Executors.newFixedThreadPool(3);

    public RequestLooper(ServerSocket serverSocket, CacheProxy proxy, CountDownLatch latch) {
        this.latch = latch;
        this.serverSocket = serverSocket;
        this.proxy = proxy;
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

    private void loop()throws Throwable{
        while (!Thread.currentThread().isInterrupted()){
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            }catch (Exception e){
                LOG.debug(e);
                continue;
            }
            Set<RequestFactory> factories = proxy.getRequestFactories();
            Request request = null;
            for(RequestFactory factory:factories){
                request = factory.from(socket,proxy);
                if (request != null){
                    break;
                }
            }
            if (request == null){
                throw new Throwable("can not get the request");
            }
            sourceExecutor.execute(request);
        }
    }
}
