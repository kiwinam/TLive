package com.testexam.charlie.tlive.main.profile.modify

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.testexam.charlie.tlive.R
import kotlinx.android.synthetic.main.activity_modify_profile.*

/**
 * 현재 로그인한 사용자의 개인 정보를 변경 할 수 있는 액티비티
 */
class ModifyProfileActivity : AppCompatActivity() , View.OnClickListener{
    private var email = ""
    private var name = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_profile)

        getIntentData()
        setClickListeners() // 클릭 리스너 설정
    }
    private fun getIntentData(){
        email  = intent.getStringExtra("email")
        name = intent.getStringExtra("name")
    }

    private fun setClickListeners(){
        modifyCloseIv.setOnClickListener(this)
        modifyConfirmIv.setOnClickListener(this)
        modifyProfileIv.setOnClickListener(this)
    }



    override fun onClick(v: View?) {
        when(v){
            modifyCloseIv->onBackPressed()  // 닫기 버튼을 누르면 액티비티를 종료한다.
            modifyProfileIv->{  // 프로필 사진을 누르면 프로필 사진을 변경할 수 있게한다.
                val profileIntent = Intent(applicationContext, ProfileCameraActivity::class.java)
                startActivity(profileIntent)
            }
            modifyConfirmIv->{  // 확인 버튼을 누르면 변경된 사항을 저장한다.

            }
        }
    }
}