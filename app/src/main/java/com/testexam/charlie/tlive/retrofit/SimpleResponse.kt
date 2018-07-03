package com.testexam.charlie.tlive.retrofit

import com.google.gson.annotations.SerializedName

/**
 * 레트로핏 응답 데이터 클래스
 */
class SimpleResponse {
    @SerializedName("success")
    var success : Boolean? = null
}