package com.example.administrator.zhihu.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseHodler(private val  view:View):RecyclerView.ViewHolder(view){

    abstract fun bind(any: Any?)
}