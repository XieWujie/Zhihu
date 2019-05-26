package com.example.administrator.zhihu.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import com.example.administrator.zhihu.data.Story
import com.example.administrator.zhihu.repository.Listing
import com.example.administrator.zhihu.repository.NewsRepository

class NewViewModel(private val repository: NewsRepository):ViewModel(){
    val liveSize = MutableLiveData<Int>()

   private val post by lazy {
       val liveData = MutableLiveData<Listing<Story>>()
       liveData.postValue(repository.post(10))
       liveData
   }

    private fun getPose(){

    }

    val posts = switchMap(post){
        it.pagedList
    }

    val networkState = switchMap(post, { it.networkState })!!
    val refreshState = switchMap(post, { it.refreshState })!!

    fun refresh() = post.value?.refresh?.invoke()

    fun retry() {
        val listing = post?.value
        listing?.retry?.invoke()
    }

}