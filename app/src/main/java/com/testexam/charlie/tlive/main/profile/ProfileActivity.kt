package com.testexam.charlie.tlive.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.OnTabSelected
import com.testexam.charlie.tlive.common.Params
import kotlinx.android.synthetic.main.activity_profile.*
import org.json.JSONObject

/**
 * 내 프로필 Activity
 *
 * 내가 올린 동영상, 가고싶다, 좋아요, 리뷰 등을 확인할 수 있다.
 * 내 계정을 팔로우 하는 사람과 시청자의 수를 확인할 수 있다.
 * 오른쪽 상단 톱니바퀴 버튼을 클릭하면 계정의 설정을 변경할 수 있는 SettingActivity 로 이동한다.
 */
class ProfileActivity : BaseActivity() , View.OnClickListener{
    private var userName = ""       // 유저의 이름
    private var userEmail = ""      // 유저의 이메일
    private var followNumber = 0    // 팔로우 숫자
    private var viewerNumber = 0    // 시청자 숫자

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sp = getSharedPreferences("login", Context.MODE_PRIVATE)

        userName = sp.getString("name", "")     // SharedPreference 에서 유저의 이름을 가져온다.
        userEmail = sp.getString("email","")    // SharedPreference 에서 유저의 이메일을 가져온다.

        getFollowNumbers()  // 팔로워와 시청자수를 가져온다.

        profileNameTv.text = userName // 유저의 이름을 TextView 에 표시한다.
        profileToolbar.title = "" // 타이틀을 사용하지 않아서 공백으로 초기화한다.

        setOnClickListeners() // 클릭 리스너들을 연결한다.
        setTabViewPagerLayout() // 탭과 뷰페이저를 설정한다.
    }

    // 클릭 리스너를 설정한다.
    // 재정의한 onClick 메소드와 클릭할 때 반응해야하는 뷰들을 연결한다.
    private fun setOnClickListeners(){
        profileCloseIv.setOnClickListener(this)
        profileSettingIv.setOnClickListener(this)
    }

    /*
     * ViewPager 에 어댑터를 설정한다.
     * ViewPager 가 변경될 때 현재 보여주고 있는 View 에 해당하는 Tab 을 선택하게 한다.
     * 탭의 개수와 들어갈 이름을 설정한다.
     * 탭을 선택할 때마다 ViewPager 가 해당 View 를 보여주도록한다.
     */
    private fun setTabViewPagerLayout(){
        val fragmentAdapter = ProfileFragmentAdapter(supportFragmentManager)    // ProfileFragmentAdapter 를 초기화한다.

        profileViewPager.adapter = fragmentAdapter      // profileViewPager 에 어댑터를 ProfileFragmentAdapter 로 설정한다
        profileViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(profileTabLayout))  // 뷰 페이저가 돌아갈 때마다 탭을 선택하게 하는 리스너

        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.movie_ko)))        // TabLayout 에 동영상을 추가한다.
        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.want_to_go_ko)))   // TabLayout 에 가고싶다를 추가한다.
        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.like_ko)))         // TabLayout 에 좋아요를 추가한다.
        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.review_ko)))       // TabLayout 에 리뷰를 추가한다.

        profileTabLayout.addOnTabSelectedListener(OnTabSelected(profileViewPager)) // 탭이 선택될 때마다 뷰 페이저가 돌아가게 하는 리스너
    }

    /*
     * 서버에서 팔로우와 시청자 수를 가져오는 메소드
     */
    private fun getFollowNumbers(){
        try{
            Thread(Runnable {   // 서버와 HTTP 통신을 하기 위한 새로운 스레드를 생성한다.
                val params = ArrayList<Params>()
                params.add(Params("email",userEmail))   // 파라미터 리스트에 현재 로그인한 유저의 이메일을 담는다.
                val result = HttpTask("getFollowNumbers.php",params).execute().get()    // 서버 getFollowNumbers.php 으로 팔로우와 시청자 수를 요청한다.
                if(result != null){
                    val resultObject = JSONObject(result)   // 결과 값을 JSON 형식으로 파싱한다.
                    followNumber = resultObject.getInt("followerNumber")  // JSON 에서 팔로우 숫자를 가져온다.
                    viewerNumber = resultObject.getInt("viewerNumber")  // JSON 에서 시청자 숫자를 가져온다.

                    runOnUiThread{
                        profileFollowNumTv.text = followNumber.toString()   // 팔로우 텍스트 뷰에 팔로우 숫자를 설정한다.
                        profileViewerNumTv.text = viewerNumber.toString()   // 시청자 텍스트 뷰에 시청자 숫자를 설정한다.
                    }
                }
            }).start()  // HTTP 스레드 시작.
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    // 뷰가 클릭될 때마다 반응하는 onClick 메소드
    // 클릭한 뷰에 따라 원하는 행동을 적는다.
    override fun onClick(v: View?) {
        when(v){
            profileCloseIv->finish()    // profileCloseIv 를 클릭하면 현재 Activity 를 종료한다.
            profileSettingIv->{         // profileSettingIv 를 클릭하면 SettingActivity 로 이동한다.
                val settingIntent = Intent(applicationContext, SettingActivity::class.java) // 셋팅 액티비티로 이동하는 인텐트
                settingIntent.putExtra("followNumber",followNumber)     // 팔로우 숫자
                settingIntent.putExtra("viewerNumber",viewerNumber)     // 시청자 숫자
                settingIntent.putExtra("name",userName)     // 유저 이름
                settingIntent.putExtra("email",userEmail)   // 유저 이메일
                settingIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(settingIntent)    // 셋팅 액티비티로 이동한다.
            }
        }
    }
}