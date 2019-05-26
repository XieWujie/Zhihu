package com.example.administrator.zhihu.repository

import com.example.administrator.zhihu.data.Story

interface NewsRepository{

    fun post(pageSize:Int):Listing<Story>
}