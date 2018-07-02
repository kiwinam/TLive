package com.testexam.charlie.tlive.join

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.login.LoginActivity
import kotlinx.android.synthetic.main.activity_join.*
import org.json.JSONObject

/**
 * 회원 가입을 진행하는 Activity.
 *
 * 회원 가입시 이름, 이메일, 비밀번호를 사용자에게 입력 받는다.
 * 입력 받은 정보를 서버에 전송하고 응답에 따라
 * Created by charlie on 2018. 5. 22
 */
class JoinActivity : BaseActivity() {
    private var name : String = ""      // 회원 이름
    private var email : String = ""     // 회원 이메일
    private var password : String =""   // 회원 비밀번호

    private var toastHandler: Handler? = Handler()  // 토스트를 실행하는 핸들러

    // 중복 이메일 경고 메시지를 띄우는 Runnable 객체
    // 회원 가입시 이미 존재하는 이메일일 경우 사용자에게 알려주기 위해 토스트 메시지를 띄운다.
    private var toastRunnable = Runnable{
        Toast.makeText(applicationContext,"이미 존재하는 이메일입니다. 다시 한 번 확인해주세요.",Toast.LENGTH_SHORT).show()   // 토스트 메시지를 띄운다.
        joinEmailLo.error = "이미 존재하는 이메일입니다."   // 이메일 TextLayout 에 에러 메시지를 표시한다.
        joinEmailEt.requestFocus()      // 이메일을 입력하는 EditTextView 로 포커스를 이동한다.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)
        joinCloseIv.setOnClickListener { onBackPressed() }  // Close 버튼을 누르면 Activity 를 종료한다.
        joinNextBtn.setOnClickListener { joinProcess() } // 가입 버튼을 누르면 회원가입 프로세스를 진행한다.
        joinPwVisibleIv.setOnClickListener { switchPasswordVisible() }  // joinPwVisibleIv 버튼을 누르게되면 비밀번호를 가리거나 보여줄 수 있다.
    }


    /*
     * 뒤로가기 버튼을 눌렀을 때 LoginActivity 를 시작하고 현재 Activity 를 종료한다.
     */
    override fun onBackPressed() {
        startActivity(Intent(applicationContext, LoginActivity::class.java))    // LoginActivity 를 시작한다.
        finish()        // 현재 Activity 를 종료한다.
    }


    /*
     * 회원가입을 진행한다.
     *
     * 회원가입 시 이메일과 패스워드를 서버에 전송하고, 동일한 이메일이 가입 되어 있지 않은 경우 회원가입을 승인한다.
     * 승인 후에는 MariaDB User 테이블에 회원 정보가 저장되고, 추가적인 정보를 입력할 수 있도록 Activity 로 이동한다.
     */
    private fun joinProcess(){
        try{
            name = joinNameEt.text.toString()       // joinNameEt 에서 이름을 가져온다.
            email = joinEmailEt.text.toString()     // joinEmailEt 에서 이메일을 가져온다.
            password = joinPwEt.text.toString()     // joinPwEt 에서 비밀번호를 가져온다.

            val paramList = ArrayList<Params>()     // 파라미터 리스트를 초기화한다.
            paramList.add(Params("name",name))      // 파라미터에 name 을 담는다.
            paramList.add(Params("email",email))    // 파라미터에 email 을 담는다.
            paramList.add(Params("password",password))  // 파라미터에 password 를 담는다.
            paramList.add(Params("age","-1"))       // 파라미터에 age 를 담는다.
            paramList.add(Params("gender","-1"))    // 파라미터에 gender 를 담는다.

            // HttpTask 클래스를 호출하여 서버에 회원가입 진행을 요청한다.
            // 요청의 결과를 joinResult 변수에 저장한다.
            val joinResult = HttpTask("join.php",paramList).execute().get()

            val resultObject = JSONObject(joinResult)   // joinResult 에 들어 있는 결과를 확인하기 위해 JSONObject 로 파싱한다.

            // 회원가입이 성공했을 때
            if(resultObject.getBoolean("success")){
                val prefs : SharedPreferences? = getSharedPreferences("login", Context.MODE_PRIVATE)    // SharedPreference 에서 login 이름으로 된 데이터를 가져온다.
                val editor = prefs!!.edit() // preference 를 변경할 수 있도록 editor 객체를 초기화한다.
                editor.putString("email",email) // email 을 저장한다.
                editor.putString("name",name)   // name 을 저장한다.
                editor.apply()      // editor 에 변경된 사항을 저장한다.
                startActivity(Intent(applicationContext, OptionalInfoActivity::class.java)) // 추가적인 정보를 기입할 수 있는 OptionalInfoActivity 로 이동한다.
                finish()    // 현재 Activity 를 종료한다.
            }else {
                // 회원가입이 이메일 중복으로 인해 실패한 경우
                if (resultObject.getInt("userNo") == -1) {  // 이메일 중복이라면 userNo 값이 -1 로 리턴된다.
                    toastHandler.run { toastRunnable }  // 이메일이 중복되었다는 토스트 메시지를 실행한다.
                }else{
                    // 서버 측 Exception 으로 실패한 경우
                    Toast.makeText(applicationContext, "회원 가입 진행 중 에러가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
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