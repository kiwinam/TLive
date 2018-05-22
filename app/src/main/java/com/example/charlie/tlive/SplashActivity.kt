package com.example.charlie.tlive

import android.content.Intent
import android.os.Bundle
import android.os.Handler

/**
 * Splash Activity
 * 일정 시간 후 메인 액티비티로 이동함
 *
 * Created by charlie on 2018. 5. 22
 */
class SplashActivity : BaseActivity() {
    private var mDelayHandler : Handler? = null
    private val SPLASH_DELAY : Long = 3000

    private val mRunnable : Runnable = Runnable {
        if(!isFinishing){
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        mDelayHandler = Handler() // 핸들러 초기화
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY) // 3초 만큼의 지연 후 mRunnable 실행
    }

    override fun onDestroy() {
        if (mDelayHandler != null){
            mDelayHandler!!.removeCallbacks(mRunnable)
        }
        super.onDestroy()
    }
}