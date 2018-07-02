package com.testexam.charlie.tlive.main.follow.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Objects;

import timber.log.Timber;

/**
 * 채팅 서비스를 연결하기 위한 BroadcastReceiver
 * 디바이스의 부팅이 완료되고 로그인이 되어 있는 상태라면 ChatService 를 시작한다.
 */
public class ChatReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)){
            SharedPreferences sp = context.getSharedPreferences("login",Context.MODE_PRIVATE);
            // 로그인이 되어 있는 경우에만 서비스를 시작한다.
            if(sp.getString("email",null) != null){
                context.startService(new Intent(context,ChatService.class));
                Timber.tag("chatReceiver").d("start chatting service");
            }
        }
    }
}
