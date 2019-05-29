package com.example.administrator.zhihu

import android.app.Application
import com.example.cache.CacheProxy
import com.example.cache.fileStrategy.SimpleFileStrategy
import com.example.cache.lifecycle.AndroidLifecycleScopeProvide

open class App:Application(){

    override fun onCreate() {
        super.onCreate()
        cacheProxy = CacheProxy(this)
        cacheProxy.registerLifecycleProvide(AndroidLifecycleScopeProvide())
        cacheProxy.start()
    }

    companion object{
        lateinit var cacheProxy: CacheProxy
    }
}