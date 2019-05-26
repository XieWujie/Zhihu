package com.example.cache.source;

public class GetSourceFactory implements SourceFactory {

    @Override
    public Source from(String type, String url, Object o) {
        if (!("Get".equals(type))){
            return null;
        }
        return new GetSource(url);
    }
}
