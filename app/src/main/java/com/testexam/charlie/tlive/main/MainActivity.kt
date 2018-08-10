package com.testexam.charlie.tlive.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.follow.FollowFragment
import com.testexam.charlie.tlive.main.live.LiveFragment
import com.testexam.charlie.tlive.main.place.PlaceFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 팔로우, 라이브, 맛집 Fragment 가 있는 MainActivity
 *
 * 탭 레이아웃을 통해 각 Fragment 를 선택할 수 있다.
 */
class MainActivity : BaseActivity(), View.OnClickListener {
    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
    external fun stringFromJNI(): String

    private var prefs : SharedPreferences? = null
    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // MainActivity 에 activity_main 레이아웃을 설정한다.
        val followFragment = FollowFragment.newInstance()
        openFragment(followFragment)
        setOnClickListeners() // MainActivity 클릭 리스너를 설정한다.

        /*
        SharedPreferences "login" 을 가져와 현재 로그인한 사용자의 정보를 불러온다.
        가져온 SharedPreferences 에서 email 정보를 'email' 변수에 저장한다.
         */
        prefs = getSharedPreferences("login", Context.MODE_PRIVATE)
        val email = prefs?.getString("email","no")
        Log.d("email", "$email..") // 현재 로그인한 사용자의 이메일

    }

    private fun openFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mainContainerFLo,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * MainActivity ClickListener 들 설정
     */
    private fun setOnClickListeners(){
        //mainToolbarSearchIv.setOnClickListener(this)
        //mainToolbarPersonIv.setOnClickListener(this)
        val bottomListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId){
                R.id.mainBNFollowMenu ->{
                    //mainToolbarTitleTv.text = getString(R.string.follow_ko)

                    val followFragment = FollowFragment.newInstance()
                    openFragment(followFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.mainBNLiveMenu->{
                    //mainToolbarTitleTv.text = getString(R.string.live_ko)

                    val liveFragment = LiveFragment.newInstance()
                    openFragment(liveFragment)

                    return@OnNavigationItemSelectedListener true
                }
                R.id.mainBNPlaceMenu->{
                    //mainToolbarTitleTv.text = getString(R.string.place_ko)

                    val placeFragment = PlaceFragment.newInstance()
                    openFragment(placeFragment)

                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
        mainBottomNV.setOnNavigationItemSelectedListener (bottomListener)
    }

    /**
     * View.OnClickListener 의 onClick Override.
     *
     * MainActivity 에서 클릭 리스너를 담당한다.
     */
    override fun onClick(v: View?) {
        /*when(v){
            *//*mainToolbarPersonIv->{
                // logout
                val edit = prefs?.edit()
                edit?.clear()
                edit?.apply()
                val profileFragment = ProfileFragment.newInstance()
                openFragment(profileFragment)

            }

            mainToolbarSearchIv->{

            }*//*
        }*/
    }

    override fun onBackPressed() {
        finish()
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
}
