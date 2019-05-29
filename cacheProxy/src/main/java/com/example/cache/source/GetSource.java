package com.example.cache.source;

import android.os.Build;
import com.example.cache.util.CacheUtil;
import com.example.cache.util.LOG;
import com.example.cache.util.Pools;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetSource implements Source {

    private String url;
    private HttpURLConnection connection;
    private InputStream inputStream;
    private static final int timeout = 4*1000;
    private static Pools.Pool<GetSource> pool = new Pools.SynchronizedPool<>(10);


    public GetSource(String url) {
       init(url);
    }

    public void init(String url){
        this.url = url;
    }
    @Override
    public String realUrl() {
        return url;
    }

    @Override
    public HttpURLConnection openConnection() throws IOException {
        if (inputStream != null){
            CacheUtil.close(inputStream);
        }
        if (connection != null){
            connection.disconnect();
        }
        connection = (HttpURLConnection) (new URL(url).openConnection());
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        return connection;
    }

    private InputStream getBlockStream(HttpURLConnection connection, long start)throws IOException{
        connection.setRequestProperty("Range", "bytes=" + start + "-");
        LOG.debug("offset"+start);
        return connection.getInputStream();
    }

    @Override
    public int read(byte[] b) throws IOException{
       return inputStream.read(b,0,b.length);
    }

    @Override
    public HttpURLConnection open(long offset) throws IOException{
        if (offset>0 && acceptRange()){
            connection = openConnection();
            inputStream = getBlockStream(connection,offset);
        }else {
            if (connection == null){
                connection = openConnection();
            }
            inputStream = connection.getInputStream();
        }
        return connection;
    }

    @Override
    public void close()throws IOException {
        if (inputStream != null){
            inputStream.close();
            inputStream = null;
        }
        if (connection != null){
            connection.disconnect();
            connection = null;
        }
        pool.release(this);
    }

    public static GetSource acquire(){
        return pool.acquire();
    }

    @Override
    public boolean acceptRange()throws IOException{
        if (connection == null){
            connection = openConnection();
        }
        return "bytes".equals(connection.getHeaderField("Accept-Ranges"));
    }

    @Override
    public long contentLength()throws IOException {
        if (connection == null){
            connection = openConnection();
        }
        return connection.getContentLength();
    }

    @Override
    public String contentType() throws IOException {
        if (connection == null){
            connection = openConnection();
        }
        return connection.getContentType();
    }
}
