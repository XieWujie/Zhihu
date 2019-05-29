package com.example.cache.source;

import java.io.File;

public interface SourceCallback {
    void callback(Exception e, File file);
}
