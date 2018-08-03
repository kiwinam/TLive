package com.testexam.charlie.tlive.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.join.JoinActivity
import com.testexam.charlie.tlive.main.MainActivity
import com.testexam.charlie.tlive.main.follow.chat.ChatService
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject


/**
 * 이메일로 로그인 진행하는 Activity
 *
 * 사용자에게 이메일과 비밀번호를 입력받고 입력 받은 정보로 로그인을 진행한다.
 * 입력한 정보가 사용자의 정보와 일치하면 로그인을 성공시키고 일치하지 않으면 입력한 정보를 다시 확인해 달라는 메시지를 띄운다.
 *
 * Created by charlie on 2018. 5. 22
 */
class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginCloseIv.setOnClickListener { onBackPressed() }     // Close 버튼을 누르게 되면 onBackPressed 메소드를 호출하여 SelectActivity 로 이동한다.
        loginBtn.setOnClickListener { loginProcess() }          // loginBtn 버튼을 누르면 입력한 정보로 로그인 처리를 진행한다.
        joinBtn.setOnClickListener{         // joinBtn 을 누르면 회원가입 Activity 로 이동한다.
            startActivity(Intent(applicationContext, JoinActivity::class.java)) // JoinActivity 를 시작한다.
            finish()    // 현재 Activity 를 종료한다.
        }
    }

    // 뒤로가기 버튼을 누르게 되면 SelectActivity 로 이동하게 한다.
    override fun onBackPressed() {
        startActivity(Intent(applicationContext, SelectActivity::class.java))
        super.onBackPressed()
    }

    /*
     * 로그인 처리를 진행하는 메소드
     *
     * 1. 사용자에게 입력받은 이메일과 비밀번호를 파라미터에 담는다.
     * 2. 서버에 로그인 처리를 요청한다.
     * 3. 서버에서 로그인이 성공하면 리턴된 값들을 SharedPreference 에 넣는다.
     * 4. 로그인이 실패하면 실패 메시지를 띄운다.
     */
    private fun loginProcess(){
        try{
            // 1. 사용자에게 입력받은 이메일과 비밀번호를 파라미터에 담는다.
            val email = loginEmailEt.text.toString() // loginEmailEt 에서 이메일을 가져온다.
            val pw = loginPwEt.text.toString()       // loginPwEt 에서 비밀번호를 가져온다.

            val paramList = ArrayList<Params>()     // 파라미터를 가지고 있는 ArrayList 를 초기화한다.
            paramList.add(Params("email",email))    // 파라미터에 email 을 넣는다.
            paramList.add(Params("password",pw))    // 파라미터에 password 를 넣는다.

            // 2. 서버에 로그인 처리를 요청한다.
            val loginResult = HttpTask("login.php",paramList).execute().get() // 서버 login.php 에 로그인 처리를 요청하고 리턴되는 결과 값을 loginResult 에 저장한다.
            val loginObject = JSONObject(loginResult)   // 리턴된 결과값을 확인하기 위해 JSONObject 형식으로 변환한다.

            // 3. 서버에서 로그인이 성공하면 리턴된 값들을 SharedPreference 에 넣는다.
            if(loginObject.getBoolean("success")){ // 로그인 처리가 성공했을 경우
                val prefs : SharedPreferences? = getSharedPreferences("login", Context.MODE_PRIVATE)    // login 을 이름으로 가진 SharedPreference 를 가져온다.
                val editor = prefs!!.edit()     // login preference 를 수정할 수 있도록 editor 객체를 선언하고 초기화한다.
                editor.putInt("userNo",loginObject.getInt("userNo"))        // userNo 을 editor 객체에 넣는다
                editor.putString("name",loginObject.getString("name"))      // name 을 editor 객체에 넣는다
                editor.putString("email",email)    // email 을 editor 객체에 넣는다
                editor.putInt("gender",loginObject.getInt("gender"))        // gender 을 editor 객체에 넣는다
                editor.putInt("age",loginObject.getInt("age"))              // age 을 editor 객체에 넣는다
                editor.putString("profileUrl",loginObject.getString("profileUrl"))  // profileUrl 을 editor 객체에 넣는다
                editor.apply()  // SharedPreference 수정 사항을 승인한다.

                startActivity(Intent(applicationContext, MainActivity::class.java)) // 메인 액티비티로 이동한다.
                startService(Intent(applicationContext, ChatService::class.java)) // Netty 1:1 채팅을 위해 채팅 서비스를 시작한다.
                finish()    // 현재 액티비티를 종료한다.

            //  4. 로그인이 실패하면 실패 메시지를 띄운다.
            }else{
                Toast.makeText(applicationContext,"로그인 회원정보가 일치하지 않습니다. 다시 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){   // 에러 발생시
            e.printStackTrace() // 에러 메시지를 로그로 표시한다.
            Toast.makeText(applicationContext,"일시적인 에러가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}