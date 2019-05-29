package com.example.cache.request;

import android.content.SharedPreferences;
import android.text.TextUtils;
import com.example.cache.cache.Cache;
import com.example.cache.source.Source;
import com.example.cache.util.CacheUtil;
import com.example.cache.util.LOG;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

abstract class AbstractRequest implements Request {


    protected Socket socket;
    protected Cache cache;
    protected Source source;
    protected ExecutorService executor;
    protected Long offset;
    private int oneSize = 4 * 1024;
    protected boolean isFreshSource;
    protected boolean sourceCompleted;
    protected long writeOffset = 0L;
    public volatile boolean isSourceRun = false;
    protected volatile boolean isCancel;
    protected RequestListener listener;
    protected boolean onlySource = false;
    protected Semaphore semaphore;
    protected SharedPreferences preferences;
    protected String header = "";
    private boolean haveCache = false;
    protected SharedPreferences.Editor editor;
    protected boolean readSucceed = false;


    public AbstractRequest(Socket socket, Cache cache, Source source, ExecutorService executor, Semaphore semaphore, SharedPreferences preferences,boolean isFreshSource, long offset) {
        this.socket = socket;
        this.cache = cache;
        this.source = source;
        this.executor = executor;
        this.semaphore = semaphore;
        this.isFreshSource = isFreshSource;
        this.offset = offset;
        this.preferences = preferences;
        this.editor = preferences.edit();
    }

    @Override
    public void run() {
        if (isCancel) {
            release();
            return;
        }
        if (socket == null) {
            onlySource = true;
        }
        if (listener != null) {
            listener.callback(State.Running);
        }
        try {
            parseHeader();
            begin();
            if (listener != null) {
                listener.callback(State.Finish);
            }
        } catch (Exception e) {
            LOG.debug(e);
            e.printStackTrace();
            if (listener != null) {
                State state = State.Error;
                state.setE(e);
                listener.callback(state);
            }
        } finally {
            if (!haveCache && !TextUtils.isEmpty(header) &&(readSucceed || onlySource)){
                editor.putString(source.realUrl(),header);
                editor.commit();
            }
            release();
        }
    }

    @Override
    public void registerListener(RequestListener listener) {
        this.listener = listener;
    }

    @Override
    public void release() {
        CacheUtil.close(cache);
        writeOffset = 0L;
        offset = 0L;
        isCancel = false;
        isSourceRun = false;
        header = "";
        socket = null;
        source = null;
        executor = null;
        isFreshSource = false;
        editor = null;
        preferences = null;
        cache = null;
        haveCache = false;
        readSucceed = false;
        sourceCompleted = false;
        if (listener != null) {
            listener.callback(State.Release);
            listener = null;
        }
        semaphore.release();
    }

    @Override
    public String realUrl() {
        return source.realUrl();
    }

    protected void sourceReady() {
        if (listener == null) {
            return;
        }
        State state = State.SourceReady;
        state.setSource(cache.source());
        listener.callback(state);
    }

    abstract void begin();


    protected void cacheRead(){
        if (onlySource) {
            return;
        }
        if (TextUtils.isEmpty(header)){
            return;
        }
        if (socket.isClosed() || !socket.isConnected()){
            return;
        }
        try {
            BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            outputStream.write(header.getBytes("UTF-8"));
            cache.seekRead(offset);
            byte[] b = new byte[oneSize];
            int length;
            while ((length = cache.read(b)) != -1) {
                outputStream.write(b, 0, length);
                outputStream.flush();
            }
            readSucceed = true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            CacheUtil.closeSocket(socket);
        }
    }

    private String parseInputStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder(30);
        String line;
        try {
            while (!TextUtils.isEmpty((line = reader.readLine()))){
                builder.append(line).append("\n");
            }
        }catch (IOException i){
            i.printStackTrace();
        }
        return builder.toString();
    }

    protected void parseHeader(){
        if (isFreshSource){
            header = parseAllHeaders();
            return;
        }
        String url = source.realUrl();
         header = preferences.getString(url,"");
        if (TextUtils.isEmpty(header)) {
            header = parseAllHeaders();
        } else {
            header = header.substring(0,header.lastIndexOf("\n")+1);
            LOG.debug("headers from cache");
            sourceCompleted = true;
            haveCache = true;
        }
    }


    protected void sourceCache(){
        try {
            isSourceRun = true;
            source.open(writeOffset);
            cache.seekWrite(writeOffset);
            byte[] b = new byte[oneSize];
            int length;
            while ((length = source.read(b)) != -1) {
                cache.write(b, length);
            }
            sourceCompleted = true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            cache.isSourceFinish(true);
            sourceReady();
            CacheUtil.close(source);
        }
    }


    private String parseAllHeaders(){
        StringBuilder builder = new StringBuilder();
        Map<String,List<String>> headers = null;
        HttpURLConnection connection = null;
        try {
            connection = source.openConnection();
            headers = connection.getHeaderFields();
        } catch (IOException e) {
            LOG.debug(e);
            return "";
        }
        if (headers == null){
            return "";
        }
        for (String key:headers.keySet()){
            if (key == null){
                builder.append(connection.getHeaderField(key)).append("\n");
            }else {
                builder.append(String.format("%s:%s", key, connection.getHeaderField(key))).append("\n");
            }
        }
        builder.append("\n");
        return builder.toString();
    }

    @Override
    public void clear() {
        isCancel = true;
    }
}

