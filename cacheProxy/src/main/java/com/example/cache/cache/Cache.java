package com.example.cache.cache;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public interface Cache extends Closeable {

    int read(byte[] b)throws IOException;
    void write(byte[] b,int length)throws IOException;
    void seekRead(long offset)throws IOException;
    void seekWrite(long offset) throws IOException;
    long available()throws IOException;
    void isSourceFinish(boolean isFinish);
    void clearContent();
    boolean isSourceFinish();
    File source();
}
