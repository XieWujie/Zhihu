package com.example.downloadhelp.DLUtil;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public class Executors {

    private Executors(){

    }

    private static Executor MAIN_EXECUTOR = new Executor(){
        private Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    };

    private static Executor DIRECT_EXECUTOR = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    public static Executor mainExecutor(){
        return MAIN_EXECUTOR;
    }

    public static Executor directExecutor(){
        return DIRECT_EXECUTOR;
    }
}
