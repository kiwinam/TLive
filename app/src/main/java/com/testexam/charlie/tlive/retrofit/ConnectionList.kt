package com.testexam.charlie.tlive.retrofit

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import retrofit2.http.Query

/**
 * Created by charlie on 2018. 5. 23
 */
interface ConnectionList {

    @Multipart
    @POST("insertReview.php")
    fun insertReview(@PartMap() partMap : Map<String, @JvmSuppressWildcards RequestBody>) : Call<SimpleResponse>
}