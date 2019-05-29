package com.example.cache.cache;

import com.example.cache.util.LOG;
import com.example.cache.util.Pools;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;

public class SourceFileCache implements Cache {

    private RandomAccessFile readFile;
    private RandomAccessFile writeFile;
    private AtomicLong cacheAvailable = new AtomicLong(0);
    private final Object readLock = new Object();
    public volatile boolean sourceFinish = false;
    private File file;
    private static Pools.Pool<SourceFileCache> pool = new Pools.SynchronizedPool<>(10);



    public SourceFileCache(File file){
        try {
            readFile = new RandomAccessFile(file,"r");
            writeFile = new RandomAccessFile(file,"rw");
            this.file = file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File source() {
        return file;
    }

    @Override
    public int read(byte[] b)throws IOException{
        cacheAvailable.intValue();
        if (cacheAvailable.intValue() >= b.length || sourceFinish){
            return readFile.read(b);
        }else {
            awaitSource();
            return readFile.read(b);
        }
    }

    private void awaitSource(){
        synchronized (readLock){
            try {
                readLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifySourceReady(){
        synchronized (readLock){
            readLock.notifyAll();
        }
    }

    @Override
    public void write(byte[] b,int length) throws IOException{
        writeFile.write(b,0,length);
        cacheAvailable.addAndGet(length);
        notifySourceReady();
    }

    @Override
    public void close() throws IOException {
        if (writeFile != null){
            writeFile.close();
            writeFile = null;
        }
        if (readFile != null){
            readFile.close();
            readFile = null;
        }
        cacheAvailable.set(0);
        sourceFinish = false;
        file = null;

    }

    @Override
    public void seekRead(long offset)throws IOException{
        readFile.seek(offset);
        cacheAvailable.addAndGet(-offset);
    }

    @Override
    public void seekWrite(long offset) throws IOException {
        writeFile.seek(offset);;
        cacheAvailable.addAndGet(offset);
        notifySourceReady();
    }

    @Override
    public long available() throws IOException{
      return writeFile.length();
    }

    @Override
    public void isSourceFinish(boolean isFinish) {
        sourceFinish = true;
    }

    @Override
    public boolean isSourceFinish() {
        return sourceFinish;
    }

    public static SourceFileCache acquire(File file){
        SourceFileCache cache = pool.acquire();
        if (cache == null){
            cache = new SourceFileCache(file);
        }
        return cache;
    }

    @Override
    public void clearContent() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write("");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
