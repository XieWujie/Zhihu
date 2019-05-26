package com.example.administrator.zhihu.data

class NewsResponse(
    val date: String,
    val stories: List<Story>,
    val top_stories: List<Story>,
    val after:String
)

data class Story(
    val ga_prefix: String,
    val id: Int,
    val images: List<String>,
    val multipic: Boolean,
    val title: String,
    val type: Int
)
