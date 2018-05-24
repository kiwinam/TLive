package com.testexam.charlie.tlive.join

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Toast
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.JoinTask
import com.testexam.charlie.tlive.login.LoginActivity
import kotlinx.android.synthetic.main.activity_join.*

/**
 * 회원 가입을 진행하는 Activity.
 *
 * Created by charlie on 2018. 5. 22
 */
class JoinActivity : BaseActivity() {
    private var name : String = ""
    private var email : String = ""
    private var password : String =""

    private var toastHandler: Handler? = Handler()

    private var toastRunnable = Runnable{
        Toast.makeText(applicationContext,"이미 존재하는 이메일입니다. 다시 한 번 확인해주세요.",Toast.LENGTH_SHORT).show()
        joinEmailLo.error = "이미 존재하는 이메일입니다."
        joinEmailEt.requestFocus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        joinCloseIv.setOnClickListener { onBackPressed() }

        joinNextBtn.setOnClickListener {
            Log.d("joinNextBtn","pressed")
            joinProcess() }

        joinPwVisibleIv.setOnClickListener { switchPasswordVisible() }
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
        name = joinNameEt.text.toString()
        email = joinEmailEt.text.toString()
        password = joinPwEt.text.toString()

        // name, email, password, age, gender
        Log.d("joinProcess",name)
        Log.d("joinProcess",email)
        Log.d("joinProcess",password)

        val result : Int = JoinTask().execute(name,email,password,"-1","-1").get()

        when (result){
            JoinTask.JOIN_OK ->{
                Log.d("Join result","ok")
                val prefs : SharedPreferences? = getSharedPreferences("login", Context.MODE_PRIVATE)
                val editor = prefs!!.edit()
                //editor.putInt("userNo",)
                editor.putString("email",email)
                editor.putString("name",name)
                editor.apply()
                startActivity(Intent(applicationContext, OptionalInfoActivity::class.java))
                finish()
            }
            JoinTask.JOIN_EXSIST -> {
                // 여기 핸들러 문제 있는듯
                toastHandler.run { toastRunnable }
                Log.e("Join result","exist")
            }
            JoinTask.JOIN_FAILED -> {
                Log.e("Join result","failed")
                Toast.makeText(applicationContext,"회원 가입 진행 중 에러가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
            else-> Log.d("result is else",result.toString()+"..")
        }
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