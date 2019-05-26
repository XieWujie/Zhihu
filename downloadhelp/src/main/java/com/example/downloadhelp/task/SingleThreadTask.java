package com.example.downloadhelp.task;


import android.util.Log;
import com.example.downloadhelp.listener.State;
import com.example.downloadhelp.request.RequestOptions;

import java.io.*;
import java.net.HttpURLConnection;

class SingleThreadTask extends AbstractTask {

    private final Object lock = new Object();
    private int downloadLength = 0;
    private RandomAccessFile file;

    public SingleThreadTask(RequestOptions options) {
        super(options);
    }

    @Override
    File begin() {
        HttpURLConnection connection = null;
        File file = getTargetFile();
        InputStream inputStream = null;
        try {
            connection = getConnection();
            downloadLength = connection.getContentLength();
            long l = file.length();
            if (isSupportMultiLoad(connection)&&l>0) {
                connection.disconnect();
                connection = getConnection();
                inputStream = getBlockStream(connection, l);
            } else {
                file.delete();
                inputStream = connection.getInputStream();
            }
            file = engine(inputStream, l);
        } catch (Exception e) {
            error(e);
            if (connection != null) {
                connection.disconnect();
            }
            close(inputStream);
        }
        return file;
    }

    @Override
    int getTotalLength() {
        return downloadLength;
    }

    @Override
    public int loadedLength() {
        if (file == null){
            return 0;
        }
        try {
            Log.d("length-",file.length()+"");
            return (int)(file.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public File engine(InputStream inputStream, long start) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        File f = null;
        State state = null;
        try {
            file = new RandomAccessFile(filePath, "rwd");
            file.seek(start);
            byte b[] = new byte[1024];
            int l = -1;
            synchronized (lock) {
                while ((l = bufferedInputStream.read(b)) != -1) {
                    file.write(b,0,l);
                    if (isPause) {
                        state = State.PAUSE;
                        listener.onState(options.getUrl(), state);
                        lock.wait();
                    }
                    if (isCancel) {
                        state = State.CANCEL;
                        listener.onState(options.getUrl(), state);
                        return null;
                    }
                }
            }
            f = new File(filePath);
            succeed(f);
        } catch (Exception e) {
            error(e);
        }
        return f;
    }

    @Override
    public void start() {
        synchronized (lock){
            isPause = false;
            listener.onState(options.getUrl(),State.RUNNING);
            lock.notifyAll();
        }
    }
}