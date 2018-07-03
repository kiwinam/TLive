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
 * 사용자의 설정을 변경할 수 있는 Activity
 *
 * 로그아웃 기능과 프로필 편집 기능 외에도 추가적으로 개발한 기능을 시현할 수 있도록 구성한다.
 */
class SettingActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        settingLogoutLo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            settingLogoutLo->{  // 로그아웃 버튼
                val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
                val editor = sp.edit()
                editor.clear().apply()  // SharedPreference 를 초기화한다.
                Toast.makeText(applicationContext,"로그아웃 되었습니다.",Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, SelectActivity::class.java))   // SelectActivity 로 이동한다.
                finish()
            }
        }
    }
}