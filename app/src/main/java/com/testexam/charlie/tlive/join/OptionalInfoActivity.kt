package com.testexam.charlie.tlive.join

import android.content.Intent
import android.os.Bundle
import com.testexam.charlie.tlive.BaseActivity
import com.testexam.charlie.tlive.MainActivity
import com.testexam.charlie.tlive.R

/**
 * 사용자에게 개인화된 추천 서비스를 제공하기 위해 추가적인 정보를 받는 Activity
 * =
 * Created by charlie on 2018. 5. 23
 */
class OptionalInfoActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optional_info)
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
}