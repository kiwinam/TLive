package com.testexam.charlie.tlive.common

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import com.testexam.charlie.tlive.retrofit_java.ConnectionListJava
import com.testexam.charlie.tlive.retrofit_java.LoginResponse
import com.testexam.charlie.tlive.retrofit_java.RetrofitConnJava
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import java.net.SocketTimeoutException

/**
 * 로그인 처리를 진행하는 클래스
 * Created by charlie on 2018. 5. 24
 */
class LoginTask(val context : Context) : AsyncTask<String,Void,Boolean>() {
    private var email : String = ""
    private var password : String = ""

    override fun doInBackground(vararg params: String?): Boolean {
        var result = false
        email = params[0].toString()
        password = params[1].toString()

        val getResponse : ConnectionListJava = RetrofitConnJava.getRetrofit().create(ConnectionListJava::class.java)

        // map 에 회원가입 필수 입력 사항 (name, email, password) 을 넣는다.
        val map : HashMap<String, RequestBody> = HashMap()
        map["email"] = RequestBody.create(MediaType.parse("multipart/form-data"),email)
        map["password"] = RequestBody.create(MediaType.parse("multipart/form-data"),password)

        val call : Call<LoginResponse> = getResponse.requestLogin(map)
        try{
            val response = call.execute()

            // 로그인이 성공적으로 진행 되었을 때
            if(response.isSuccessful){
                if(response.body().success){
                    Log.d("LoginTask-response","true")
                    result = true
                    val userNo : Int = response.body().userNo
                    val name : String = response.body().name
                    val gender : Int = response.body().gender
                    val age : Int = response.body().age
                    val profileUrl : String ?= response.body().profileUrl

                    val prefs : SharedPreferences? = context.getSharedPreferences("login", Context.MODE_PRIVATE)
                    val editor = prefs!!.edit()
                    editor.putInt("userNo",userNo)
                    editor.putString("name",name)
                    editor.putString("email",email)
                    editor.putInt("gender",gender)
                    editor.putInt("age",age)
                    editor.putString("profileUrl",profileUrl)
                    editor.apply()
                }else{
                    Log.d("LoginTask-response","false")

                }
            // 로그인에 실패하였을 때
            }else{
                Log.d("response","failed")
            }
        }catch (e : SocketTimeoutException){
            e.printStackTrace()
        }
        return result
    }
}