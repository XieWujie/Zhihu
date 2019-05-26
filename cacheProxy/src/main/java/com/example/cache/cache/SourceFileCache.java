package com.example.cache.cache;

import com.example.cache.util.LOG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicLong;

public class SourceFileCache implements Cache {

    private RandomAccessFile readFile;
    private RandomAccessFile writeFile;
    private AtomicLong cacheAvailable = new AtomicLong(0);
    private final Object readLock = new Object();
    public volatile boolean sourceFinish = false;


    public SourceFileCache(File file){
        try {
            readFile = new RandomAccessFile(file,"r");
            writeFile = new RandomAccessFile(file,"rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
                LOG.debug("await");
                readLock.wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifySourceReady(){
        synchronized (readLock){
            LOG.debug("notify");
            readLock.notifyAll();
        }
    }

    @Override
    public void write(byte[] b,int length) throws IOException{
        writeFile.write(b,0,length);
        if (cacheAvailable.addAndGet(length)>10*1024){
            notifySourceReady();
        }
    }

    @Override
    public void seekRead(long offset)throws IOException{
        readFile.seek(offset);
        cacheAvailable.addAndGet(-offset);
    }

    @Override
    public void seekWrite(long offset) throws IOException {
        writeFile.seek(offset);
        cacheAvailable.addAndGet(offset);
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
}
