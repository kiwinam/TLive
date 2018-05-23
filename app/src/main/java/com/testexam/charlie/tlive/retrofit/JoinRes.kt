package com.testexam.charlie.tlive.retrofit

import com.google.gson.annotations.SerializedName

/**
 * Created by charlie on 2018. 5. 23..
 */

data class Join(
        @SerializedName("name") val name : String,
        @SerializedName("email") val email :String,
        @SerializedName("password") val password : String
        )

data class Result(
        val success : Boolean , val userNum : Int
)