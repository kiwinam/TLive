package com.testexam.charlie.tlive.common

import android.os.AsyncTask
import android.util.Log
import com.testexam.charlie.tlive.retrofit_java.ConnectionListJava
import com.testexam.charlie.tlive.retrofit_java.JoinResponseJava
import com.testexam.charlie.tlive.retrofit_java.RetrofitConnJava
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import java.net.SocketTimeoutException

/**
 * 회원 가입을 진행하는 클래스
 *
 * Created by charlie on 2018. 5. 24
 */
class JoinTask : AsyncTask<String,Void,Int>() {

    companion object {
        /*
        회원가입 처리 결과를 알려주는 resultCode
         1000 - 회원가입 정상 처리
         2000 - 이미 존재하는 아이디
         4000 - 회원가입 처리 불가능
         */
        val JOIN_OK = 1000
        val JOIN_EXSIST = 2000
        val JOIN_FAILED = 4000
    }

    override fun doInBackground(vararg params: String?): Int {
        val name = params[0].toString()
        val email = params[1].toString()
        val password = params[2].toString()
        val age = params[3].toString()
        val gender = params[4].toString()

        Log.d("JoinTask name",name)
        Log.d("JoinTask email",email)
        Log.d("JoinTask password",password)
        Log.d("JoinTask age",age)
        Log.d("JoinTask gender",gender)

        val getResponse : ConnectionListJava = RetrofitConnJava.getRetrofit().create(ConnectionListJava::class.java)

        var resultCode : Int = -1

        // map 에 회원가입 필수 입력 사항 (name, email, password) 을 넣는다.
        val map : HashMap<String, RequestBody> = HashMap()
        map.put("name", RequestBody.create(MediaType.parse("multipart/form-data"),name))
        map.put("email", RequestBody.create(MediaType.parse("multipart/form-data"),email))
        map.put("password", RequestBody.create(MediaType.parse("multipart/form-data"),password))
        map.put("age", RequestBody.create(MediaType.parse("multipart/form-data"),age))
        map.put("gender", RequestBody.create(MediaType.parse("multipart/form-data"),gender))

        // 서버에 map 에 넣은 회원 정보를 토대로 회원 가입을 요청한다.
        // 회원가입 요청이 정상적으로 전달 되고 join.php 을 읽는데 오류가 없으면 response 는 success 이다.
        // 회원가입 요청시 이미 존재하는 이메일로 회원 가입 요청을 한다면 response.body().message 에서 -1 을 전달한다.
        // 회원가입이 정상적으로 이루어지면 userNo 가 전달되고, 전달된 userNo 와 email 을 SharedPreferences 에 저장한다.
        val call : Call<JoinResponseJava> = getResponse.requestJoin(map)
        try{
            val response = call.execute()
            resultCode = if(response.isSuccessful){
                if(response.body().message.toString() == "-1"){
                    JOIN_EXSIST // 같은 이메일로 이미 회원가입이 되어있음.
                }else{
                    JOIN_OK // 회원가입 성공
                }
            }else{
                JOIN_FAILED // 회원 가입 진행 중 에러 발생
            }
        }catch (e : SocketTimeoutException){
            resultCode = JOIN_FAILED // 회원 가입 진행 중 에러 발생
            e.printStackTrace()
        }
        Log.d("JoinTask resultCode",resultCode.toString()+"..")
        return resultCode
    }
}