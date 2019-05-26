package com.example.administrator.zhihu

import android.app.Application
import com.example.cache.CacheProxy

open class App:Application(){

    override fun onCreate() {
        super.onCreate()
        cacheProxy = CacheProxy(this)
        cacheProxy.start()
    }

    companion object{
        lateinit var cacheProxy: CacheProxy
    }
}