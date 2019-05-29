package com.example.cache.source;

import com.example.cache.Release;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

public interface Source extends Closeable {

    int read(byte[] b)throws IOException;

    HttpURLConnection open(long offset)throws IOException;

    boolean acceptRange()throws IOException;

    long contentLength()throws IOException;

    String contentType()throws IOException;

    String realUrl();

    HttpURLConnection openConnection()throws IOException;
}

