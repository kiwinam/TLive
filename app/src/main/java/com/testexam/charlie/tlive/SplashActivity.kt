package com.testexam.charlie.tlive

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.login.SelectActivity
import com.testexam.charlie.tlive.main.MainActivity
import com.testexam.charlie.tlive.main.follow.chat.ChatService

/**
 * Splash Activity
 * 일정 시간 후 SelectActivity 로 이동함
 *
 * Created by charlie on 2018. 5. 22
 */
class SplashActivity : BaseActivity() {
    private var mDelayHandler : Handler? = null
    private val splashDelay: Long = 3000
    private val mRunnable : Runnable = Runnable {
        val prefs : SharedPreferences? = getSharedPreferences("login", Context.MODE_PRIVATE)

        val nextIntent =
        if(prefs!!.getString("email","none") == "none"){
            Intent(applicationContext, SelectActivity::class.java)
        }else{
            Intent(applicationContext, MainActivity::class.java)
        }


        if(prefs.getString("email","none") !== "none"){
            startService(Intent(applicationContext, ChatService::class.java))
        }
        startActivity(nextIntent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mDelayHandler = Handler() // 핸들러 초기화
        mDelayHandler!!.postDelayed(mRunnable, splashDelay) // 3초 만큼의 지연 후 mRunnable 실행
    }

    override fun onDestroy() {
        if (mDelayHandler != null){
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }


}