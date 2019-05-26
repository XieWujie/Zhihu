package com.example.cache.source;

public interface SourceFactory {

    Source from(String type,String url,Object o);

}
