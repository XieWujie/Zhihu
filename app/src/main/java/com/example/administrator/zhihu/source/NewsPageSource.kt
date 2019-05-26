package com.example.administrator.zhihu.source

import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.example.administrator.zhihu.data.Api
import com.example.administrator.zhihu.data.NetworkState
import com.example.administrator.zhihu.data.NewsResponse
import com.example.administrator.zhihu.data.Story
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

class NewsPageSource(
    private val api: Api,
    private val retryExecutor: Executor
):PageKeyedDataSource<Int,Story>(){

    private var retry:(()->Any)? = null

    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Story>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        try {
            val data = api.topNews().execute().body()
            val top = data?.top_stories ?: emptyList()
            val s = data?.stories ?: emptyList()
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(s, null, 0)
        }catch (e:IOException){
            retry = {
                loadInitial(params,callback)
            }
            val error = NetworkState.error(e.message?:"unknow error")
            initialLoad.postValue(error)
            networkState.postValue(error)
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Story>) {
        networkState.postValue(NetworkState.LOADING)
        initialLoad.postValue(NetworkState.LOADING)
        try {
            val newKey = params.key+1
            val dateBefore = getDateBefore(newKey)
            val data = api.nextNews(dateBefore).execute().body()
            val s = data?.stories ?: emptyList()
            retry = null
            networkState.postValue(NetworkState.LOADED)
            initialLoad.postValue(NetworkState.LOADED)
            callback.onResult(s,newKey)
        }catch (e:IOException){
            retry = {
                loadAfter(params,callback)
            }
            val error = NetworkState.error(e.message?:"unknow error")
            initialLoad.postValue(error)
            networkState.postValue(error)
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Story>) {

    }

    private fun getDateBefore(before:Int):String{
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyyMMdd")
        calendar.add(Calendar.DATE,-before)
        return  format.format(calendar.time)
    }
}