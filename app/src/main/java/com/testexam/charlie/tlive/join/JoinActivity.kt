package com.testexam.charlie.tlive.join

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import com.testexam.charlie.tlive.BaseActivity
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.login.LoginActivity
import com.testexam.charlie.tlive.retrofit_java.ConnectionListJava
import com.testexam.charlie.tlive.retrofit_java.JoinResponseJava
import com.testexam.charlie.tlive.retrofit_java.RetrofitConnJava
import kotlinx.android.synthetic.main.activity_join.*
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call

/**
 * 회원 가입을 진행하는 Activity.
 *
 * Created by charlie on 2018. 5. 22
 */
class JoinActivity : BaseActivity() {

    private var toastHandler: Handler? = Handler()

    private var toastRunnable = Runnable{
        Toast.makeText(applicationContext,"이미 존재하는 이메일입니다. 다시 한 번 확인해주세요.",Toast.LENGTH_SHORT).show()
        joinEmailLo.error = "이미 존재하는 이메일입니다."
        joinEmailEt.requestFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        joinCloseIv.setOnClickListener {
            onBackPressed()
        }

        joinNextBtn.setOnClickListener {
            joinProcess()
        }

        joinPwVisibleIv.setOnClickListener {
            switchPasswordVisible()
        }
    }


    /**
     * 뒤로가기 버튼을 눌렀을 때 LoginActivity 를 시작하고 현재 Activity 를 종료한다.
     */
    override fun onBackPressed() {
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finish()
    }


    /**
     * 회원가입을 진행한다.
     *
     * 회원가입 시 이메일과 패스워드를 서버에 전송하고, 동일한 이메일이 가입 되어 있지 않은 경우 회원가입을 승인한다.
     * 승인 후에는 MariaDB User 테이블에 회원 정보가 저장되고, 추가적인 정보를 입력할 수 있도록 Activity 로 이동한다.
     */
    private fun joinProcess(){
        var name = joinNameEt.text.toString()
        var email = joinEmailEt.text.toString()
        var password = joinPwEt.text.toString()

        //var getResponse : ConnectionList = RetrofitConn.getRetrofit().create(ConnectionList::class.java)

        var getResponse : ConnectionListJava = RetrofitConnJava.getRetrofit().create(ConnectionListJava::class.java)

        var map : HashMap<String, RequestBody> = HashMap()

        map.put("name",RequestBody.create(MediaType.parse("multipart/form-data"),name))
        map.put("email",RequestBody.create(MediaType.parse("multipart/form-data"),email))
        map.put("password",RequestBody.create(MediaType.parse("multipart/form-data"),password))

        Log.d("requestJoin","pressed")
        Log.d("name",name)
        Log.d("email",email)
        Log.d("password",password)

        var call : Call<JoinResponseJava> = getResponse.requestJoin(map)
        //call.enqueue(Callback<JoinResponseJava>)
        Thread({
            val response = call.execute()
            if(response.isSuccessful){
                Log.d("response","success")
                Log.d("success",response.body().success.toString())
                Log.d("message",response.body().message.toString())
                if(response.body().message.toString().equals("-1")){
                    toastHandler?.post(toastRunnable)
                }else{
                    startActivity(Intent(applicationContext,OptionalInfoActivity::class.java))
                    finish()
                }
            }else{
                Log.d("response","failed")
            }
        }).start()
    }

    /**
     * Password visible icon action
     * 패스워드 보임 여부를 변경하는 메서드.
     *
     * 회원가입 시 패스워드 보임 여부를 변경한다. 패스워드 inputType 이 1 일 경우 Text 형식이고 225 일경우 Password 형식이다.
     * 이를 이용해 패스워드 입력 창의 InputType 을 변경한다.
     */
    private fun switchPasswordVisible(){

        if(joinPwEt.inputType == 1){ // inputType == 1 : Text , inputType == 225 : WebPassword
            // inputType 이 텍스트일 경우엔 패스워드 형식으로 변경한다.

            joinPwEt.setRawInputType(225) // inputType 을 225(WebPassword) 로 변경한다.
            joinPwEt.transformationMethod = PasswordTransformationMethod.getInstance() // transformationMethod 를 패스워드 형식으로 변경한다.
            joinPwVisibleIv.setImageDrawable(getDrawable(R.drawable.ic_visibility_off_gray_24dp)) // joinPwVisibleIv 의 이미지를 변경한다.
        }else{
            // 패스워드 형식일 때는 텍스트 형식으로 변경한다.
            joinPwEt.setRawInputType(1) // inputType 을 225(WebPassword) 로 변경한다.
            joinPwEt.transformationMethod = HideReturnsTransformationMethod.getInstance()  // transformationMethod 를 텍스트 형식으로 변경한다.
            joinPwVisibleIv.setImageDrawable(getDrawable(R.drawable.ic_visibility_gray_24dp)) // joinPwVisibleIv 의 이미지를 변경한다.
        }
        joinPwEt.setSelection(joinPwEt.text.length) // 패스워드 입력창의 포커스 커서 위치를 패스워드의 길이만큼 준다.
    }
}