package com.example.administrator.zhihu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.administrator.zhihu.data.Story
import com.example.administrator.zhihu.databinding.StoryItemBinding

class StoryHolder(private val binding:StoryItemBinding):BaseHodler(binding.root){

    override fun bind(any: Any?) {
        if (any is Story){
            binding.story = any
        }
    }

    companion object {

        fun create(parent:ViewGroup):StoryHolder{
            val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return StoryHolder(binding)
        }
    }
}