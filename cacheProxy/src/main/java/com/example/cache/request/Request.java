package com.example.cache.request;

public interface Request extends Runnable {

    void clear();

    String type();

    String realUrl();

}
