package com.testexam.charlie.tlive.main.profile

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.view.View
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.OnTabSelected
import kotlinx.android.synthetic.main.activity_profile.*

/**
 *
 */
class ProfileActivity : BaseActivity() , View.OnClickListener{

    private var userName = ""
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sp = getSharedPreferences("login", Context.MODE_PRIVATE)

        userName = sp.getString("name", "")
        userEmail = sp.getString("email","")

        profileToolbar.title = ""
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setOnClickListeners()
        setTabViewPagerLayout()

    }

    private fun setOnClickListeners(){
        profileCloseIv.setOnClickListener(this)
        profileSettingIv.setOnClickListener(this)
    }

    /*
     *
     */
    private fun setTabViewPagerLayout(){

        val fragmentAdapter = ProfileFragmentAdapter(supportFragmentManager)

        profileViewPager.adapter = fragmentAdapter
        profileViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(profileTabLayout))  // 뷰 페이저가 돌아갈 때마다 탭을 선택하게 하는 리스너

        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.movie_ko)))
        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.want_to_go_ko)))
        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.like_ko)))
        profileTabLayout.addTab(profileTabLayout.newTab().setText(getString(R.string.chat_room_ko)))

        profileTabLayout.addOnTabSelectedListener(OnTabSelected(profileViewPager)) // 탭이 선택될 때마다 뷰 페이저가 돌아가게 하는 리스너
    }

    override fun onClick(v: View?) {
        when(v){
            profileCloseIv->finish()
            profileSettingIv->{

            }
        }
    }
}