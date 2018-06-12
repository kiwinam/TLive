package com.testexam.charlie.tlive.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.LoginTask
import com.testexam.charlie.tlive.join.JoinActivity
import com.testexam.charlie.tlive.main.MainActivity
import com.testexam.charlie.tlive.main.follow.chat.ChatService
import kotlinx.android.synthetic.main.activity_login.*


/**
 * Created by charlie on 2018. 5. 22
 */
class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginCloseIv.setOnClickListener {
            onBackPressed()
        }

        loginBtn.setOnClickListener {
            loginProcess()
        }

        joinBtn.setOnClickListener{
            startActivity(Intent(applicationContext, JoinActivity::class.java))
            finish()
        }
    }



    override fun onBackPressed() {
        startActivity(Intent(applicationContext, SelectActivity::class.java))
        super.onBackPressed()
    }

    private fun loginProcess(){
        val email = loginEmailEt.text.toString()
        val pw = loginPwEt.text.toString()

        val result : Boolean = LoginTask(applicationContext).execute(email,pw).get()

        if(result){
            Log.d("LoginActivity","result true")
            startActivity(Intent(applicationContext, MainActivity::class.java)) // 메인 액티비티로 이동함.
            startService(Intent(applicationContext, ChatService::class.java)) // 채팅 서비스 시작
            finish()
        }else{
            Toast.makeText(applicationContext,"로그인 회원정보가 일치하지 않습니다. 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
            Log.d("LoginActivity","result false")
        }
    }
}