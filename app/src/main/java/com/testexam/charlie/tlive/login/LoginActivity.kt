package com.testexam.charlie.tlive.login

import android.content.Intent
import android.os.Bundle
import com.testexam.charlie.tlive.BaseActivity
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.join.JoinActivity
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
            // 로그인 프로세스 진행
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
}