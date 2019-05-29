package com.example.administrator.zhihu.data

import com.bumptech.glide.RequestBuilder
import com.example.administrator.zhihu.App
import com.example.cache.util.LOG
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Path
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.SocketAddress

interface Api{

    @GET("api/4/news/latest")
    fun topNews():Call<NewsResponse>

    @GET("api/4/news/before/{dateBefore}")
    fun nextNews(@Path("dateBefore") dateBefore:String):Call<NewsResponse>

}

class Ret{

    companion object {

        private const val BASE_URL = "http://news-at.zhihu.com/"

        fun create() = create(HttpUrl.parse(BASE_URL)!!)
        fun create(url:HttpUrl):Api{
            val client = OkHttpClient.Builder()
                .proxy(Proxy(Proxy.Type.HTTP,InetSocketAddress("127.0.0.1",App.cacheProxy.port)))
                .addInterceptor { chain ->
                    val r = Request.Builder()
                        .addHeader("Connection","close")
                        .url(chain.request().url())
                        .build();
                    chain.proceed(r)
                }
                .build();
            return Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api::class.java)
        }
    }
}