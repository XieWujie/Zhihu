package com.example.administrator.zhihu.repository

import androidx.lifecycle.Transformations
import androidx.paging.toLiveData
import com.example.administrator.zhihu.data.Api
import com.example.administrator.zhihu.data.Story
import com.example.administrator.zhihu.source.NewsPageFactory
import java.util.concurrent.Executor

class NewsByNetRepository(private val api: Api,private val ioExecutor: Executor):NewsRepository{

    override fun post(pageSize: Int): Listing<Story> {
        val factory = NewsPageFactory(api,ioExecutor)
        val livePageList = factory.toLiveData(pageSize,fetchExecutor = ioExecutor)
        val refreshState = Transformations.switchMap(factory.sourceLiveData){
            it.initialLoad
        }
        return Listing(
            pagedList = livePageList,
            networkState = Transformations.switchMap(factory.sourceLiveData){it.networkState},
            retry = {
                factory.sourceLiveData.value?.retryAllFailed()
            },
            refreshState = refreshState,
            refresh = {
                factory.sourceLiveData.value?.invalidate()
            }
        )
    }
}