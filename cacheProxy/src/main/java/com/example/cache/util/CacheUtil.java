package com.example.cache.util;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheUtil {

    private static AtomicInteger count = new AtomicInteger(0);
    private static Writer writer = new StringWriter();

    public static int countSource(int value){
        return count.addAndGet(value);
    }

   public static String encode(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding url", e);
        }
    }

   public static String decode(String url) {
        try {
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error decoding url", e);
        }
    }

    public static void close(Closeable...closeables){
       for (Closeable closeable:closeables){
           try {
               closeable.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }

    public static void closeSocket(Socket socket){
       if (socket == null){
           return;
       }
       if (socket.isClosed()){
           return;
       }
       if (socket.isConnected()||!socket.isClosed()){
           close(socket);
       }
    }

}
