package com.example.cache.fileStrategy;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class SimpleFileStrategy implements FileStrategy {

    private String parentPath;

    public SimpleFileStrategy(Context context,String base) {
        if (isExternalCanUser()){
            parentPath = context.getExternalCacheDir().getAbsolutePath() + "/"+base+"/";
        }else {
            if (context != null) {
                parentPath = context.getExternalCacheDir().getAbsolutePath() + "/"+base+"/";
            }else {
                throw new RuntimeException("need provide context");
            }
        }
    }

    @Override
    public String parentPath() {
        return parentPath;
    }

    @Override
    public File get(String url) {
        int lastIndex  = url.lastIndexOf("?");
        int begin = url.lastIndexOf("/");
        String fileName = url.substring(begin == -1?0:begin,lastIndex==-1?url.length():lastIndex);
        File file = new File(parentPath,fileName+".txt");
        checkFile(file);
        return file;
    }

    protected void checkFile(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("can not create file: "+file);
            }
        }
    }

    private static boolean isExternalCanUser(){
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
