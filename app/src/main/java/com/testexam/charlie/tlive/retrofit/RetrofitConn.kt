package com.testexam.charlie.tlive.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by charlie on 2018. 5. 23
 */
class RetrofitConn {
    companion object {
        fun getRetrofit() : Retrofit {
            val gson : Gson = GsonBuilder()
                    .setLenient()
                    .create()
            return Retrofit.Builder()
                    .baseUrl("http://13.125.64.135/app/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
        }
    }

}