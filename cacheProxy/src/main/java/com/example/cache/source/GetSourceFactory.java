package com.example.cache.source;

public class GetSourceFactory implements SourceFactory {

    @Override
    public Source from(String type, String url, Object o) {
        if (!("GET".equalsIgnoreCase(type))){
            return null;
        }
        GetSource source = GetSource.acquire();
        if (source == null){
            return new GetSource(url);
        }
        source.init(url);
        return source;
    }
}
