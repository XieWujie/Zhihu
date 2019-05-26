package com.example.downloadhelp.task;

import com.example.downloadhelp.listener.State;
import com.example.downloadhelp.request.RequestOptions;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

public class MultiThreadTask extends AbstractTask {

    private final List<Object> segmentLockList = new ArrayList<Object>();
    private ExecutorService executor;
    private int contentLength;
    private final Object mainLock = new Object();
    private List<LoadMessage> loadMessages = new ArrayList<>();


    public MultiThreadTask(RequestOptions options, ExecutorService executor) {
        super(options);
        this.executor = executor;
        segmentLockList.add(mainLock);
    }


    @Override
    File begin() {
        HttpURLConnection connection = null;
        try {
            connection = getConnection();
            contentLength = connection.getContentLength();
            if (isSupportMultiLoad(connection)) {
                supportMultiStrategy(connection);
            } else {
                notSupportMultiStrategy(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(e);
            if (connection != null) {
                connection.disconnect();
            }
        }
        succeed(getTargetFile());
        return getStateFile();
    }


    @Override
    public int loadedLength() {
        int sum = 0;
        try {
            for (int i = 0, size = loadMessages.size(); i < size; i++) {
                LoadMessage message = loadMessages.get(i);
                int length = (int) (message.file.getFilePointer() - message.start);
                sum+=length;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return sum;
    }

    @Override
    int getTotalLength() {
        return contentLength;
    }

    @Override
    public void pause() {
        super.pause();
        listener.onState(options.getUrl(), State.PAUSE);
    }

    @Override
    public void cancel() {
        super.cancel();
        listener.onState(options.getUrl(), State.CANCEL);
    }


    private void supportMultiStrategy(HttpURLConnection connection) {
        int size = options.getSegment();
        int segmentLength = contentLength / size;
        int firstStart = 0, firstEnd = 0;
        CountDownLatch latch = new CountDownLatch(size - 1);
        for (int i = 0; i < size; i++) {
            int start = i * segmentLength;
            int end;
            if (i < size - 1) {
                end = start + segmentLength - 1;
            } else {
                end = contentLength;
            }
            if (i == 0) {
                firstStart = start;
                firstEnd = end;
            } else {
                final Object lock = new Object();
                segmentLockList.add(lock);
                NewSegmentTask task = new NewSegmentTask(start, end, lock, latch);
                executor.execute(task);
            }
        }
        thread(firstStart, firstEnd, mainLock);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void notSupportMultiStrategy(HttpURLConnection connection) {
        try {
            InputStream inputStream = connection.getInputStream();
            RandomAccessFile file = new RandomAccessFile(filePath,"rwd");
            file.seek(0);
            loadMessages.add(new LoadMessage(0,file));
            engine(inputStream, 0, file,mainLock);
        } catch (Exception e) {
            e.printStackTrace();
            error(e);
        }
    }

    private void engine(InputStream inputStream,int start, RandomAccessFile file, final Object lock) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);;
        try {
            byte b[] = new byte[1024];
            synchronized (lock) {
                int l = -1;
                while ((l = bufferedInputStream.read(b)) != -1) {
                    file.write(b, 0, l);
                    if (isPause) {
                        lock.wait();
                    }
                    if (isCancel) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            error(e);
        }finally {
            if (file != null){
                try {
                    putProperty(String.valueOf(start),String.valueOf(file.getFilePointer()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void thread(int start, int end, Object lock) {
        HttpURLConnection connection = getConnection();
        int g = getIntProperty(String.valueOf(start));
        int loadStart = g== 0?start:g;
        InputStream inputStream = getBlockStream(connection, loadStart, end);
        try {
            RandomAccessFile file = new RandomAccessFile(filePath,"rwd");
            file.seek(loadStart);
            loadMessages.add(new LoadMessage(0,file));
            engine(inputStream, start,file, lock);
            connection.disconnect();
        } catch (Exception e) {
            error(e);
        }
    }

    @Override
    public void start() {
        isPause = false;
        listener.onState(options.getUrl(),State.RUNNING);
        for (int i = 0, size = segmentLockList.size(); i < size; i++) {
            final Object l = segmentLockList.get(i);
            synchronized (l) {
                l.notifyAll();
            }
        }
        listener.onState(options.getUrl(), State.RUNNING);
    }

    private class NewSegmentTask implements Runnable {
        private int start;
        private int end;
        private final Object segmentLock;
        private CountDownLatch latch;

        public NewSegmentTask(int start, int end, Object segmentLock, CountDownLatch latch) {
            this.start = start;
            this.end = end;
            this.segmentLock = segmentLock;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                thread(start, end, segmentLock);
            } finally {
                latch.countDown();
            }
        }
    }

    private class LoadMessage{
        int start;
        RandomAccessFile file;

        public LoadMessage(int start, RandomAccessFile file) {
            this.start = start;
            this.file = file;
        }

        public LoadMessage() {
        }
    }
}
