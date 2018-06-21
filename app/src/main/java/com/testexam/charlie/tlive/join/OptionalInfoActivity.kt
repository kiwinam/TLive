package com.testexam.charlie.tlive.join

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.main.MainActivity
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.retrofit_java.ConnectionListJava
import com.testexam.charlie.tlive.retrofit_java.ResultResponse
import com.testexam.charlie.tlive.retrofit_java.RetrofitConnJava
import kotlinx.android.synthetic.main.activity_optional_info.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import java.net.SocketTimeoutException

/**
 * 사용자에게 개인화된 추천 서비스를 제공하기 위해 추가적인 정보를 받는 Activity
 * =
 * Created by charlie on 2018. 5. 23
 */
class OptionalInfoActivity : BaseActivity(), View.OnClickListener {
    private var gender : Int = -1
    private var age : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optional_info)
        setOnClickListeners()
    }

    private fun setOnClickListeners(){
        optGenderMaleBtn.setOnClickListener(this)
        optGenderFeMaleBtn.setOnClickListener(this)
        optAge10Btn.setOnClickListener(this)
        optAge20Btn.setOnClickListener(this)
        optAge30Btn.setOnClickListener(this)
        optAge40Btn.setOnClickListener(this)
        optAge50Btn.setOnClickListener(this)
        optSubmitBtn.setOnClickListener(this)
        optSkipBtn.setOnClickListener(this)
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }



    override fun onClick(v: View?) {
        when (v){
            optGenderMaleBtn -> selectGender(0)
            optGenderFeMaleBtn -> selectGender(1)
            optAge10Btn -> selectAge(0)
            optAge20Btn -> selectAge(1)
            optAge30Btn -> selectAge(2)
            optAge40Btn -> selectAge(3)
            optAge50Btn -> selectAge(4)
            optSubmitBtn -> optInfoSubmit()
            optSkipBtn -> onBackPressed()
        }
    }

    /**
     * 추가 정보를 서버로 제출하는 함수.
     */
    @SuppressLint("LogNotTimber")
    private fun optInfoSubmit(){
        val getResponse : ConnectionListJava = RetrofitConnJava.getRetrofit().create(ConnectionListJava::class.java)
        val pref : SharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE)
        val email = pref.getString("email","none")

        // map 에 추가 정보를 (gender, age) 을 넣는다.
        val map : HashMap<String, RequestBody> = HashMap()
        map["email"] = RequestBody.create(MediaType.parse("multipart/form-data"),email.toString())
        map["gender"] = RequestBody.create(MediaType.parse("multipart/form-data"),gender.toString())
        map["age"] = RequestBody.create(MediaType.parse("multipart/form-data"),age.toString())
        Thread({
            val call : Call<ResultResponse> = getResponse.requestUpdateInfo(map)
            try{
                val response = call.execute()
                if(response.isSuccessful){
                    Log.d("response","success")
                    Log.d("success",response.body()!!.success.toString())
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                }else{
                    Log.d("response","failed")
                    Toast.makeText(applicationContext,"추가 정보 기입 중 에러가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }catch (e : SocketTimeoutException){
                Toast.makeText(applicationContext,"네트워크의 상태가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }).start()

    }

    /**
     * 추가 정보 중 성별을 선택하는 함수
     * 남자 = 0 , 여자 = 1 로 pos 값을 넘겨준다.
     */
    private fun selectGender(pos : Int){
        gender = pos
        Log.d("gender",gender.toString()+".")
        if(pos == 0){ // 남자를 선택한 경우
            optGenderMaleBtn.setTextColor(Color.parseColor("#ffffff"))
            optGenderMaleBtn.setBackgroundResource(R.drawable.sp_left_solid_rect)

            optGenderFeMaleBtn.setTextColor(Color.parseColor("#FFB300"))
            optGenderFeMaleBtn.setBackgroundResource(R.drawable.sp_right_line_rect)
        }else{
            optGenderMaleBtn.setTextColor(Color.parseColor("#FFB300"))
            optGenderMaleBtn.setBackgroundResource(R.drawable.sp_left_line_rect)

            optGenderFeMaleBtn.setTextColor(Color.parseColor("#ffffff"))
            optGenderFeMaleBtn.setBackgroundResource(R.drawable.sp_right_solid_rect)
        }
    }

    /**
     * 연령대를 선택하는 함수
     * 연령대를 저장하는 age 변수는 , 10대 = 0 , 20대 = 1 , 30대 = 2, 40대 = 3, 50대 이상 = 4 의 값을 갖는다.
     */
    private fun selectAge(pos : Int){
        age = pos
        Log.d("age",age.toString()+".")
        // 모든 연령대 버튼 초기화, 주황색 글씨와 선만 있는 버튼으로 변경
        optAge10Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge10Btn.setBackgroundResource(R.drawable.sp_left_line_rect)
        optAge20Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge20Btn.setBackgroundResource(R.drawable.sp_middle_line_rect)
        optAge30Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge30Btn.setBackgroundResource(R.drawable.sp_middle_line_rect)
        optAge40Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge40Btn.setBackgroundResource(R.drawable.sp_middle_line_rect)
        optAge50Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge50Btn.setBackgroundResource(R.drawable.sp_right_line_rect)

        // 선택된 버튼만 하얀색 글씨와 색이 채워진 버튼으로 변경
        when(pos){
            0 -> { optAge10Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge10Btn.setBackgroundResource(R.drawable.sp_left_solid_rect)  }
            1 -> { optAge20Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge20Btn.setBackgroundResource(R.drawable.sp_middle_solid_rect)  }
            2 -> { optAge30Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge30Btn.setBackgroundResource(R.drawable.sp_middle_solid_rect)  }
            3 -> { optAge40Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge40Btn.setBackgroundResource(R.drawable.sp_middle_solid_rect)  }
            4 -> { optAge50Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge50Btn.setBackgroundResource(R.drawable.sp_right_solid_rect)  }
        }
    }
}