package com.example.administrator.zhihu.data

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Path

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
            return Retrofit.Builder()
                .baseUrl(url)
                .client(OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api::class.java)
        }
    }
}