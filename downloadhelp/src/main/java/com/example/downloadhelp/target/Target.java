package com.example.downloadhelp.target;

import com.example.downloadhelp.request.RequestOptions;

public interface Target<Resource> {

    void onReady(RequestOptions options,Resource resource);

}
