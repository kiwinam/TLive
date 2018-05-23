package com.testexam.charlie.tlive.retrofit

import com.google.gson.annotations.SerializedName

/**
 * Created by charlie on 2018. 5. 23
 */
class JoinResponse {

    @SerializedName("success")
    var success : Boolean? = null
    @SerializedName("userNo")
    var userNo : Int? = null
/*
    fun getSuccess(): Boolean {
        return success
    }*/
}
