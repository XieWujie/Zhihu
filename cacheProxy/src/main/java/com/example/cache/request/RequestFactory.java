package com.example.cache.request;

import com.example.cache.CacheProxy;

import java.net.Socket;

public interface RequestFactory {

    Request from(RequestConfig config,Socket socket, CacheProxy proxy);

}
