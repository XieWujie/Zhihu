package com.example.cache.request;

import android.text.TextUtils;
import com.example.cache.cache.Cache;
import com.example.cache.source.Source;
import com.example.cache.util.LOG;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

abstract class AbstractRequest implements Request {


    protected Socket socket;
    protected Cache cache;
    protected Source source;
    protected ExecutorService executor;
    protected Long offset;
    private int oneSize = 2*1024;
    protected boolean isFreshSource;
    protected boolean sourceCompleted;
    protected long writeOffset = 0L;
    protected String header = "";
    public volatile boolean isSourceRun = false;
    protected long contentLength;
    protected long available;


    public AbstractRequest(Socket socket, Cache cache, Source source, ExecutorService executor, boolean isFreshSource, long offset) {
        this.socket = socket;
        this.cache = cache;
        this.source = source;
        this.executor = executor;
        this.isFreshSource = isFreshSource;
        this.offset = offset;
    }


    @Override
    public void run() {
        try {
            if (TextUtils.isEmpty(header)&&!isSourceRun){
                header = parse();
            }
            begin();
        } catch (IOException e) {
            LOG.debug(e);
            e.printStackTrace();
        }finally {
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String realUrl() {
        return source.realUrl();
    }

    abstract void begin() throws IOException;


    protected void cacheRead(BufferedOutputStream outputStream) throws IOException {
        cache.seekRead(offset);
        byte[] b = new byte[oneSize];
        int length;
        while ((length = cache.read(b)) != -1) {
            outputStream.write(b, 0, length);
            outputStream.flush();
        }
    }



    protected void sourceCache() throws IOException {
        isSourceRun = true;
        source.open(writeOffset);
        cache.seekWrite(writeOffset);
        byte[] b = new byte[oneSize];
        int length;
        while ((length = source.read(b)) != -1) {
            cache.write(b, length);
        }
        cache.isSourceFinish(true);
        sourceCompleted = true;
    }

    protected String parse()throws IOException{
        boolean acceptRange = source.acceptRange();
        String contentType = source.contentType();
        contentLength = source.contentLength();
        boolean typeKnown = !TextUtils.isEmpty(contentType);
        available = cache.available();
        if (!isFreshSource){
            writeOffset = available;
        }
        if (contentLength == available){
            sourceCompleted = true;
        }
        long length = cache.isSourceFinish() ? available: contentLength;
        return new StringBuilder()
                .append(offset >= 0 &&acceptRange ? "HTTP/1.1 206 PARTIAL CONTENT\n" : "HTTP/1.1 200 OK\n")
                .append("Accept-Ranges: bytes\n")
                .append(length>=0? String.format("Content-Length: %d\n", contentLength) : "")
                .append(acceptRange ? String.format("Content-Range: bytes %d-%d/%d\n", offset, length - 1, contentLength) : "")
                .append(typeKnown ? String.format("Content-Type: %s\n", contentType) : "")
                .append("\n") // headers end
                .toString();
    }
}

