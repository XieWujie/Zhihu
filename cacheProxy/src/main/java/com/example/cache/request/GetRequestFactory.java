package com.example.cache.request;

import android.text.TextUtils;
import com.example.cache.CacheProxy;
import com.example.cache.util.CacheUtil;
import com.example.cache.cache.Cache;
import com.example.cache.cache.SourceFileCache;
import com.example.cache.source.Source;
import com.example.cache.source.SourceFactory;
import com.example.cache.util.LOG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetRequestFactory implements RequestFactory {

    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-");
    private static final Pattern URL_PATTERN = Pattern.compile("GET /(.*) HTTP");

    @Override
    public Request from(Socket socket, CacheProxy proxy) {
        try {
            String parse = parse(socket.getInputStream());
            String url = findUri(parse);
            if (TextUtils.isEmpty(url)){
                return null;
            }else {
                GetRequest getRequest = null;
                url = CacheUtil.decode(url);
                Request request = proxy.getRequests().get(url);
                long offset = findRangeOffset(parse);
                if (request!= null && "Get".equals(request.type())){
                    getRequest = (GetRequest)request;
                    LOG.debug("isSourceRun"+getRequest.isSourceRun);
                    getRequest.setSocket(socket);
                    getRequest.setOffset(offset);
                }
                if (getRequest != null){
                    return getRequest;
                }
                Set<SourceFactory> sourceFactories = proxy.getSourceFactories();
                Source source = null;
                for (SourceFactory factory:sourceFactories){
                     source = factory.from("Get",url,null);
                }
                if (source == null){
                    return null;
                }
                Cache cache = new SourceFileCache(proxy.getFileStrategy().get(url));
                getRequest = new GetRequest(socket,cache,source,proxy.getCacheExecutor(),false,offset);
                return getRequest;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String parse(InputStream inputStream)throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder(30);
        String line;
        while (!TextUtils.isEmpty((line = reader.readLine()))){
            builder.append(line).append("\n");
        }
        return builder.toString();
    }

    private long findRangeOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            return Long.parseLong(rangeValue);
        }
        return 0;
    }

    private String findUri(String request) {
        Matcher matcher = URL_PATTERN.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
