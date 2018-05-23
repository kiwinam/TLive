package com.testexam.charlie.tlive.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.testexam.charlie.tlive.BaseActivity;
import com.testexam.charlie.tlive.R;

import org.jetbrains.annotations.Nullable;

/**
 * Created by charlie on 2018. 5. 22
 */

public class SelectActivity extends BaseActivity {
    private final static String CLIENT_ID = "l6RpovZAnv9FUFxEgcHC";
    private final static String CLIENT_SECRET = "QtfYhb7L0M";
    private final static String CLIENT_NAME = "T Live";
    public static OAuthLogin mOAuthLoginModule;
    private Context context;
    private long mLastTimeBackPressed;
    @SuppressLint("HandlerLeak")
    public OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success){
                String accessToken = mOAuthLoginModule.getAccessToken(context);
                String refreshToken = mOAuthLoginModule.getRefreshToken(context);
                long expiresAt = mOAuthLoginModule.getExpiresAt(context);
                String tokenType = mOAuthLoginModule.getTokenType(context);
                Log.d("accessToken",accessToken+".");
                Log.d("refreshToken",refreshToken+".");
                Log.d("expiresAt",expiresAt+".");
                Log.d("tokenType",tokenType+".");
            }else{
                String errorCode = mOAuthLoginModule.getLastErrorCode(context).getCode();
                String errorDesc = mOAuthLoginModule.getLastErrorDesc(context);
                Toast.makeText(context, "errorCode:"+errorCode+", errordesc:"+errorDesc,Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        context = this;
        setNaver();

        Button emailBtn = findViewById(R.id.selectEmailBtn);
        Button naverBtn = findViewById(R.id.selectNaverBtn);

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });

        naverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOAuthLoginModule.startOauthLoginActivity(SelectActivity.this,mOAuthLoginHandler);
            }
        });
    }

    private void setNaver(){
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(this,CLIENT_ID,CLIENT_SECRET,CLIENT_NAME);
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - mLastTimeBackPressed < 1500){
            finish();
            return;
        }
        mLastTimeBackPressed = System.currentTimeMillis();
        Snackbar.make(getCurrentFocus(),"한 번 더 누르면 종료됩니다",Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        context = null;
        super.onDestroy();
    }
}
