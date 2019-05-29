package com.example.cache.request;

import android.text.TextUtils;
import com.example.cache.CacheProxy;
import com.example.cache.cache.Cache;
import com.example.cache.cache.SourceFileCache;
import com.example.cache.source.Source;
import com.example.cache.source.SourceFactory;
import com.example.cache.util.LOG;

import java.net.Socket;
import java.util.Set;

public class GetRequestFactory implements RequestFactory {

    @Override
    public Request from(RequestConfig config, Socket socket, CacheProxy proxy) {
        if (!("GET".equalsIgnoreCase(config.getMethod()))) {
            return null;
        }
        String url = config.getUrl();if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("the url can not be empty");
        }
        Set<SourceFactory> sourceFactories = proxy.getSourceFactories();
        Source source = null;
        for (SourceFactory factory : sourceFactories) {
            source = factory.from("GET", url, null);
        }
        if (source == null) {
            LOG.debug("can not find the source of get request");
            return null;
        }
        Cache cache = SourceFileCache.acquire(proxy.getFileStrategy().get(url));
        GetRequest request = GetRequest.acquire();
        request = GetRequest.acquire();
        if (request == null) {
            request = new GetRequest(socket, cache, source, proxy.getCacheExecutor(),proxy.getSemaphore(),proxy.getPreferences(), proxy.isFreshSource(), config.getOffset());
        } else {
            request.init(socket, cache, source, proxy.getCacheExecutor(),proxy.getSemaphore(),proxy.getPreferences(), proxy.isFreshSource(), config.getOffset());
        }
        return request;
    }
}
