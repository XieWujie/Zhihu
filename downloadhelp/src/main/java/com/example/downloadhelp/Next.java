package com.example.downloadhelp;

public interface Next<T extends Next> {

    void setNext(T t);

    T getNext();
}
