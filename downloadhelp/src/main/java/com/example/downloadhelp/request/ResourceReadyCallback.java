package com.example.downloadhelp.request;

import com.example.downloadhelp.target.Target;

public class ResourceReadyCallback<Resource> implements Runnable {

    private Resource resource;
    private RequestOptions requestOptions;
    private Target<Resource> target;

    public ResourceReadyCallback(Resource resource, RequestOptions requestOptions, Target<Resource> target) {
        this.resource = resource;
        this.requestOptions = requestOptions;
        this.target = target;
    }

    @Override
    public void run() {
        target.onReady(requestOptions,resource);
    }
}
