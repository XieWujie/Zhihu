package com.example.administrator.zhihu.source

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.example.administrator.zhihu.data.Api
import com.example.administrator.zhihu.data.Story
import java.io.IOException
import java.util.concurrent.Executor


class NewsPageFactory(
    private val api: Api,
    private val ioExecutor: Executor
):DataSource.Factory<Int,Story>(){
    val sourceLiveData = MutableLiveData<NewsPageSource>()

    override fun create(): DataSource<Int, Story> {
        val source = NewsPageSource(api,ioExecutor)
        sourceLiveData.postValue(source)
        return source
    }

}