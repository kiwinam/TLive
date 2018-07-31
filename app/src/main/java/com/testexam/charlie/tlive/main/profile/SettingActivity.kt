package com.testexam.charlie.tlive.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.login.SelectActivity
import com.testexam.charlie.tlive.main.profile.modify.ModifyProfileActivity

import kotlinx.android.synthetic.main.activity_setting.*

/**
 * 사용자의 설정을 변경할 수 있는 Activity
 *
 * 로그아웃 기능과 프로필 편집 기능 외에도 추가적으로 개발한 기능을 시현할 수 있도록 구성한다.
 */
class SettingActivity : BaseActivity(), View.OnClickListener {
    private var email = ""
    private var name = ""
    private var followNumber = 0
    private var viewerNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        getIntentData() // 필요한 정보를 인텐트로 전달받는다.
        setClickListeners() // 클릭 리스너를 설정한다.
    }

    // 필요한 정보를 인텐트에서 가져오는 메소드
    private fun getIntentData(){
        email = intent.getStringExtra("email")  // 이메일을 가져온다.
        name = intent.getStringExtra("name")    // 이름을 가져온다.
        followNumber = intent.getIntExtra("followNumber",0)  //
        viewerNumber = intent.getIntExtra("viewerNubmer",0)
    }

    // 클릭 리스너를 설정하는 메소드
    private fun setClickListeners(){
        settingLogoutLo.setOnClickListener(this)
        settingCloseIv.setOnClickListener(this)
        settingModifyLo.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            settingCloseIv->onBackPressed() // 설정 닫기 버튼, 액티비티를 종료한다.
            settingLogoutLo->{  // 로그아웃 버튼
                val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
                val editor = sp.edit()
                editor.clear().apply()  // SharedPreference 를 초기화한다.
                Toast.makeText(applicationContext,"로그아웃 되었습니다.",Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, SelectActivity::class.java))   // SelectActivity 로 이동한다.
                finish()
            }
            settingModifyLo->{  // 수정 버튼
                val modifyIntent = Intent(applicationContext,ModifyProfileActivity::class.java)
                modifyIntent.putExtra("email",email)
                modifyIntent.putExtra("name",name)
                startActivity(modifyIntent)
            }
        }
    }
}