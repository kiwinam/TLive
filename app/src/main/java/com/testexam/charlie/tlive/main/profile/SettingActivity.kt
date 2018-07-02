package com.testexam.charlie.tlive.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.login.SelectActivity

import kotlinx.android.synthetic.main.activity_setting.*

/**
 * 설정 Activity
 */
class SettingActivity : BaseActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        settingLogoutLo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            settingLogoutLo->{
                val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
                val editor = sp.edit()
                editor.clear().apply()
                Toast.makeText(applicationContext,"로그아웃 되었습니다.",Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, SelectActivity::class.java))
                finish()
            }
        }
    }
}