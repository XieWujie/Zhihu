package com.example.administrator.zhihu

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class Function{

    companion object {
        @JvmStatic
        @BindingAdapter("imageSrc")
        fun setImage(view: ImageView, src:String?){
            if (src == null)return
            val new = App.cacheProxy.getProxyUrl(src)
            Glide.with(view).load(new).diskCacheStrategy(DiskCacheStrategy.NONE).into(view)
        }
    }
}