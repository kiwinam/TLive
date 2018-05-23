package com.testexam.charlie.tlive.retrofit;

import java.util.Observable;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by charlie on 2018. 5. 23..
 */

public interface ApiService {
  //  @POST("join.php")
//    Observable<Result> join(@Query("name") String name, @Query("email") String email, @Query("password") String password);

    class Factory{
        public static ApiService create(){
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("")
                    .build();
            return retrofit.create(ApiService.class);

        }
    }
}
