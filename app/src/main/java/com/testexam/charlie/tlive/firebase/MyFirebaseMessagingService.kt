package com.testexam.charlie.tlive.firebase

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.main.follow.chat.ChatService
import org.json.JSONObject
import java.util.*

/**
 * 파이어베이스 서비스
 *
 */
@SuppressLint("LogNotTimber")
class MyFirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService(){
    override fun onNewToken(token : String?) {
        Log.d("onNewToken",token)
    }
    override fun onMessageReceived(remoteMessage : RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Log.e("onMessageReceived",remoteMessage.toString())
        val data =  remoteMessage!!.data["key"]
        Log.e("onMessageReceived", "$data..")

        startChattingService()  // 채팅 서비스 시작
    }

    /*
     * 소켓 연결이 끊어진 디바이스에 FCM 알림을 통해 소켓을 다시 연결하는 메소드
     */
    private fun startChattingService(){
        val sp = getSharedPreferences("login", Context.MODE_PRIVATE)
        val email = sp.getString("email",null)
        if(!email.isNullOrEmpty()){
            Log.e("startChattingService","startService")
            if(!isServiceRunning()){
                startService(Intent(applicationContext, ChatService::class.java))
            }
        }else{
            Log.e("startChattingService","email is empty")
        }
    }

    // 서비스의 실행 여부를 확인하는 메소드
    private fun isServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (runningServiceInfo in Objects.requireNonNull(activityManager).getRunningServices(Integer.MAX_VALUE)) {
            println(runningServiceInfo.service.className)
            if (ChatService::class.java.name == runningServiceInfo.service.className) {
                Log.e("isServiceRunning", "already running ChatService")
                return true
            }
        }
        Log.e("isServiceRunning", "new ChatService")
        return false   // 실행중이지 않다.
    }
}