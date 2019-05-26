package com.example.cache.source;

import java.io.IOException;
import java.net.HttpURLConnection;

public interface Source {

    int read(byte[] b)throws IOException;

    void open(long offset)throws IOException;

    boolean acceptRange()throws IOException;

    long contentLength()throws IOException;

    String contentType()throws IOException;

    void close()throws IOException;

    String realUrl();

    HttpURLConnection openConnection()throws IOException;
}

