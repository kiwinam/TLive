package com.testexam.charlie.tlive.retrofit_java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.testexam.charlie.tlive.retrofit.RetrofitConn;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by charlie on 2018. 5. 23..
 */

public class RetrofitConnJava {
    private static String BASE_URL = "http://13.125.64.135/app/";

    public static Retrofit getRetrofit(){

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(RetrofitConnJava.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}

