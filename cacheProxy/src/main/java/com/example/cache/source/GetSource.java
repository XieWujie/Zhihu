package com.example.cache.source;

import android.os.Build;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetSource implements Source {

    private String url;
    private HttpURLConnection connection;
    private InputStream inputStream;

    public GetSource(String url) {
        this.url = url;
    }

    @Override
    public String realUrl() {
        return url;
    }

    @Override
    public HttpURLConnection openConnection() throws IOException {
        if (connection != null){
            connection.disconnect();
        }
        connection = (HttpURLConnection) (new URL(url).openConnection());
        return connection;
    }

    private InputStream getBlockStream(HttpURLConnection connection, long start)throws IOException{
        connection.setRequestProperty("Range", "bytes=" + start + "-");
        return connection.getInputStream();
    }

    @Override
    public int read(byte[] b) throws IOException{
       return inputStream.read(b,0,b.length);
    }

    @Override
    public void open(long offset) throws IOException{
        if (offset>0 && acceptRange()){
            connection = openConnection();
            inputStream = getBlockStream(connection,offset);
        }else {
            if (connection == null){
                connection = openConnection();
            }
            inputStream = connection.getInputStream();
        }
    }

    @Override
    public void close()throws IOException {
        if (connection != null){
            connection.disconnect();
            connection = null;
        }
        if (inputStream != null){
            inputStream.close();
            inputStream = null;
        }
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
        if (Build.VERSION.SDK_INT>24) {
            return connection.getContentLengthLong();
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
