package com.testexam.charlie.tlive.join

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.main.MainActivity
import kotlinx.android.synthetic.main.activity_optional_info.*

/**
 * 사용자에게 개인화된 추천 서비스를 제공하기 위해 추가적인 정보를 받는 Activity
 *
 * 추가적인 정보는 성별, 연령대를 받는다.
 * Created by charlie on 2018. 5. 23
 */
class OptionalInfoActivity : BaseActivity(), View.OnClickListener {
    private var gender : Int = -1       // 성별 변수
    private var age : Int = -1          // 연령대 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optional_info)
        setOnClickListeners()   // 클릭 리스너를 설정한다.
    }

    // 오버라이딩한 onClick 에 클릭 리스너를 연결한다.
    private fun setOnClickListeners(){
        optGenderMaleBtn.setOnClickListener(this)
        optGenderFeMaleBtn.setOnClickListener(this)
        optAge10Btn.setOnClickListener(this)
        optAge20Btn.setOnClickListener(this)
        optAge30Btn.setOnClickListener(this)
        optAge40Btn.setOnClickListener(this)
        optAge50Btn.setOnClickListener(this)
        optSubmitBtn.setOnClickListener(this)
        optSkipBtn.setOnClickListener(this)
    }

    // 뒤로 가기 버튼을 눌렀을 때 MainActivity 로 돌아가게한다.
    override fun onBackPressed() {
        startActivity(Intent(applicationContext, MainActivity::class.java)) // MainActivity 실행
        finish()    // 현재 액티비티 종료
    }

    // OnClickListener 에서 onClick 메소드를 오버라이딩한다.
    override fun onClick(v: View?) {
        when (v){
            // 성별을 선택하면 선택된 성별은 주황색으로 선택되지 않은 성별을 하얀색 배경을 갖는다.
            optGenderMaleBtn -> selectGender(0)
            optGenderFeMaleBtn -> selectGender(1)

            // 연령대를 선택하면 선택된 연령대는 주황색으로 선택되지 않은 성별은 하얀색 배경을 갖는다.
            optAge10Btn -> selectAge(0)     // 10대
            optAge20Btn -> selectAge(1)     // 20대
            optAge30Btn -> selectAge(2)     // 30대
            optAge40Btn -> selectAge(3)     // 40대
            optAge50Btn -> selectAge(4)     // 50대 이상
            optSubmitBtn -> optInfoSubmit()     // 추가 정보를 서버에 제출하는 메소드를 호출한다.
            optSkipBtn -> onBackPressed()       // 추가 정보 기입을 원하지 않는 경우 MainActivity 로 이동한다.
        }
    }

    /**
     * 추가 정보를 서버로 제출하는 메소드
     */
    @SuppressLint("LogNotTimber")
    private fun optInfoSubmit(){
        val pref : SharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE)
        val email = pref.getString("email","none")      // SharedPreference 에서 현재 로그인한 사용자의 이메일을 가져온다.

        // 서버에 전송할 파라미터를 설정한다.
        val paramList = ArrayList<Params>()     // 파라미터들을 가지고 있는 ArrayList 초기화
        paramList.add(Params("email",email))    // email 을 파라미터에 추가한다.
        paramList.add(Params("gender",gender.toString()))      // gender 를 파라미터에 추가한다.
        paramList.add(Params("age",age.toString()))            // age 를 파라미터에 추가한다.
        try{
            val result = HttpTask("UpdateOptionalInfo.php",paramList).execute().get()
            if(result == "1"){
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }else{
                Toast.makeText(applicationContext,"추가 정보 기입 중 에러가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Toast.makeText(applicationContext,"네트워크의 상태가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * 추가 정보 중 성별을 선택하는 함수
     * 남자 = 0 , 여자 = 1 로 pos 값을 넘겨준다.
     */
    private fun selectGender(pos : Int){
        gender = pos // gender 변수를 현재 선택한 position 값으로 설정한다.

        // 남자를 선택한 경우
        // 남자 버튼을 오렌지 색으로 변경한다.
        // 여자 버튼을 하얀색으로 변경한다.
        if(pos == 0){
            optGenderMaleBtn.setTextColor(Color.parseColor("#ffffff"))
            optGenderMaleBtn.setBackgroundResource(R.drawable.sp_left_solid_rect)

            optGenderFeMaleBtn.setTextColor(Color.parseColor("#FFB300"))
            optGenderFeMaleBtn.setBackgroundResource(R.drawable.sp_right_line_rect)
        // 여자를 선택한 경우
        // 여자 버튼을 오렌지 색으로 변경한다.
        // 남자 버튼을 하얀색으로 변경한다.
        }else{
            optGenderMaleBtn.setTextColor(Color.parseColor("#FFB300"))
            optGenderMaleBtn.setBackgroundResource(R.drawable.sp_left_line_rect)

            optGenderFeMaleBtn.setTextColor(Color.parseColor("#ffffff"))
            optGenderFeMaleBtn.setBackgroundResource(R.drawable.sp_right_solid_rect)
        }
    }

    /**
     * 연령대를 선택하는 함수
     * 연령대를 저장하는 age 변수는 , 10대 = 0 , 20대 = 1 , 30대 = 2, 40대 = 3, 50대 이상 = 4 의 값을 갖는다.
     */
    private fun selectAge(pos : Int){
        age = pos
        // 모든 연령대 버튼 초기화, 주황색 글씨와 선만 있는 버튼으로 변경
        optAge10Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge10Btn.setBackgroundResource(R.drawable.sp_left_line_rect)
        optAge20Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge20Btn.setBackgroundResource(R.drawable.sp_middle_line_rect)
        optAge30Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge30Btn.setBackgroundResource(R.drawable.sp_middle_line_rect)
        optAge40Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge40Btn.setBackgroundResource(R.drawable.sp_middle_line_rect)
        optAge50Btn.setTextColor(Color.parseColor("#FFB300"))
        optAge50Btn.setBackgroundResource(R.drawable.sp_right_line_rect)

        // 선택된 버튼만 하얀색 글씨와 색이 채워진 버튼으로 변경
        when(pos){
            0 -> { optAge10Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge10Btn.setBackgroundResource(R.drawable.sp_left_solid_rect)  }
            1 -> { optAge20Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge20Btn.setBackgroundResource(R.drawable.sp_middle_solid_rect)  }
            2 -> { optAge30Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge30Btn.setBackgroundResource(R.drawable.sp_middle_solid_rect)  }
            3 -> { optAge40Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge40Btn.setBackgroundResource(R.drawable.sp_middle_solid_rect)  }
            4 -> { optAge50Btn.setTextColor(Color.parseColor("#ffffff"))
                optAge50Btn.setBackgroundResource(R.drawable.sp_right_solid_rect)  }
        }
    }
}