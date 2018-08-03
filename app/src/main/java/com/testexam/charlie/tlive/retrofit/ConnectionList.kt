package com.testexam.charlie.tlive.retrofit

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PartMap
import retrofit2.http.Query

/**
 * 레트로핏에서 서버와 통신할 때 연결하는 리스트를 정의해놓은 인터페이스
 *
 * Created by charlie on 2018. 5. 23
 */
interface ConnectionList {
    // 리뷰 올리기
    @Multipart
    @POST("insertReview.php")
    fun insertReview(@PartMap() partMap : Map<String, @JvmSuppressWildcards RequestBody>) : Call<SimpleResponse>

    // 프로필 사진 변경하기
    @Multipart
    @POST("updateProfile.php")
    fun updateProfile(@PartMap() partMap : Map<String, @JvmSuppressWildcards RequestBody>) : Call<SimpleResponse>
}