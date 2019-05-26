package com.example.downloadhelp.task;

import com.example.downloadhelp.listener.DLProgressListener;
import com.example.downloadhelp.listener.State;
import com.example.downloadhelp.listener.StateListener;
import com.example.downloadhelp.request.RequestOptions;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

abstract class AbstractTask implements Task {

    protected transient boolean isPause = false;
    protected transient boolean isCancel = false;
    protected StateListener listener;
    protected RequestOptions options;
    protected File stateFile;
    protected String filePath;
    protected static int SUCCEED = 200;
    private static String DOWNLOAD_STATE = "download_state";
    protected Properties getProperty = new Properties();
    protected DLProgressListener progressListener;

    public AbstractTask(RequestOptions options) {
        this.options = options;
        filePath = options.getParentPath() + options.getFileName();
        try {
            getProperty.load(new FileInputStream(getStateFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void recycler() {
        this.listener = null;
        this.options = null;
        this.stateFile = null;
        this.getProperty = null;
        this.progressListener = null;
    }

    @Override
    public void pause() {
        isPause = true;
    }

    protected boolean isSupportMultiLoad(HttpURLConnection connection) {
        return "bytes".equals(connection.getHeaderField("Accept-Ranges"));
    }

    protected InputStream getBlockStream(HttpURLConnection connection, long start, long end) {
        if (start > end) {
            throw new RuntimeException("start can not > end");
        }
        connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
        try {
            return connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            error(e);
        }
        return null;
    }

    abstract int loadedLength();

    abstract int getTotalLength();

    @Override
    public void registerProgressListener(DLProgressListener listener) {
        this.progressListener = listener;
    }

    @Override
    public void freshProgress() {
        if (progressListener != null) {
            progressListener.onProgress(loadedLength(), getTotalLength());

        }
    }

    protected HttpURLConnection getConnection() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) (new URL(options.getUrl()).openConnection());
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            error(e);
        }
        return null;
    }

    protected InputStream getBlockStream(HttpURLConnection connection, long start) throws IOException {
        connection.setRequestProperty("Range", "bytes=" + start + "-");
        return connection.getInputStream();
    }

    protected void writeFinish() {
        putProperty(DOWNLOAD_STATE, String.valueOf(SUCCEED));
    }

    protected void error(Exception e) {
        e.printStackTrace();
        State state = State.ERROR;
        state.setException(e);
        listener.onState(options.getUrl(), state);
    }

    abstract File begin();

    @Override
    public File call() throws Exception {
        listener.onState(options.getUrl(),State.RUNNING);
        if (isFileFinish()) {
            File file = new File(filePath);
            succeed(file,true);
            int length = (int) file.length();
            progressListener.onProgress(length,length);
            return file;
        }
        return begin();
    }

    protected void succeed(File file) {
        succeed(file,true);
    }
    protected void succeed(File file,boolean write) {
        State state = State.FINISH;
        state.setResult(file);
        listener.onState(options.getUrl(), state);
        if (write){
            writeFinish();
        }
    }

    protected File getTargetFile() {
        File f = new File(filePath);
        checkFile(f);
        return f;
    }

    protected boolean isFileFinish() {
        if (getIntProperty(DOWNLOAD_STATE) == SUCCEED) {
            return true;
        } else {
            return false;
        }
    }


    protected String getStringProperty(String key) {
        return getProperty.getProperty(key);
    }

    protected int getIntProperty(String key) {
        String v = getProperty.getProperty(key);
        if (v == null) {
            return 0;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected File getStateFile() {
        if (stateFile == null) {
            String path =filePath + "state_of.txt";
            stateFile = new File(path);
            checkFile(stateFile);
        }
        return stateFile;
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    protected void putProperty(String key, String value) {
        try {
            Properties putProperty = new Properties();
            FileOutputStream outputStream = new FileOutputStream(getStateFile());
            putProperty.setProperty(key, String.valueOf(value));
            putProperty.store(outputStream, "文件下载指针");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void checkFile(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registerStateListener(StateListener listener) {
        this.listener = listener;
    }

}
