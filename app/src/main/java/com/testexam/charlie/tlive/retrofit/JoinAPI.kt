package com.testexam.charlie.tlive.retrofit

import retrofit2.Call

/**
 * Created by charlie on 2018. 5. 23..
 */
interface JoinAPI {
    fun requestJoin(name : String, email : String, password : String) : Call<JoinResponse>
}