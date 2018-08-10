package com.testexam.charlie.tlive.main.profile

import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.login.SelectActivity
import com.testexam.charlie.tlive.main.follow.chat.Chat
import com.testexam.charlie.tlive.main.follow.chat.ChatService
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
        followNumber = intent.getIntExtra("followNumber",0)
        viewerNumber = intent.getIntExtra("viewerNumber",0)
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
                logout()    // 로그아웃을 진행한다.
            }
            settingModifyLo->{  // 수정 버튼
                val modifyIntent = Intent(applicationContext,ModifyProfileActivity::class.java)
                modifyIntent.putExtra("email",email)
                modifyIntent.putExtra("name",name)
                startActivity(modifyIntent)
            }
        }
    }

    /*
     * 로그아웃 메소드
     *
     * 로그아웃 시 디바이스에 저장된 데이터를 삭제하고 서버에 저장된 파이어베이스 토큰 정보를 변경한다.
     * 채팅 서비스를 종료한다.
     * SelectActivity 로 이동한다.
     */
    private fun logout(){
        try{
            val sp = getSharedPreferences("login", Context.MODE_PRIVATE)

            // 파이어 베이스 토큰 정보 변경
            val paramList = ArrayList<Params>()
            paramList.add(Params("email",sp.getString("email",null)))
            HttpTask("logoutToken.php",paramList).execute() // 파이어베이스 토큰 정보를 'logout' 으로 변경한다.


            Thread{
                FirebaseInstanceId.getInstance().deleteInstanceId()
                //FirebaseInstanceId.getInstance(FirebaseApp.initializeApp(applicationContext)!!).deleteInstanceId()
            }

            val editor = sp.edit()
            editor.clear().apply()  // SharedPreference 를 초기화한다.
            Toast.makeText(applicationContext,"로그아웃 되었습니다.",Toast.LENGTH_SHORT).show()

            stopService(Intent(applicationContext, ChatService::class.java)) // 채팅 서비스 종료

            startActivity(Intent(applicationContext, SelectActivity::class.java))   // SelectActivity 로 이동한다.
            finish()
        }catch (e:NetworkErrorException){
            e.printStackTrace()
            Toast.makeText(applicationContext, "네트워크 환경이 불안정합니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            e.printStackTrace()
        }

    }
}