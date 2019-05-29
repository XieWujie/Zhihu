package com.example.administrator.zhihu

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.example.cache.request.RequestConfig


class Function{

    companion object {
        val handler = Handler(Looper.getMainLooper());

        @JvmStatic
        @BindingAdapter("imageSrc")
        fun setImage(view: ImageView, src:String?){
            if (src == null){
                return
            }
            val config = RequestConfig()
            config.method = "GET"
            config.offset = 0L;
            config.url = src
//            App.cacheProxy.onlyFromFile(config){e,file->
//                file?.apply {
//                    handler.post {
//                        Glide.with(view).load(file).diskCacheStrategy(DiskCacheStrategy.NONE).into(view)
//                    }
//                }
//            }
            val new = App.cacheProxy.getProxyUrl(src)
            Glide.with(view).load(new).diskCacheStrategy(DiskCacheStrategy.NONE).into(view)
        }
    }
}