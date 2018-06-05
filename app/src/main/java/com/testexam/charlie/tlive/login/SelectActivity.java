package com.testexam.charlie.tlive.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crust87.texturevideoview.widget.TextureVideoView;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.testexam.charlie.tlive.common.BaseActivity;
import com.testexam.charlie.tlive.main.MainActivity;
import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.common.JoinTask;

import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by charlie on 2018. 5. 22
 */

public class SelectActivity extends BaseActivity {
    private final static String CLIENT_ID = "l6RpovZAnv9FUFxEgcHC";
    private final static String CLIENT_SECRET = "QtfYhb7L0M";
    private final static String CLIENT_NAME = "T Live";
    String name ="";
    String email ="";
    String age="";
    String gender="";
    public Map<String, String> mNaverUserInfoMap;
    public static OAuthLogin mOAuthLoginModule;

    private Context context;
    private long mLastTimeBackPressed;

    @SuppressLint("HandlerLeak")
    public OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if(success){
                new RequestApiTask().execute();
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

        setBackgroundVideo();

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

    /**
     * 네아로 (네이버 아이디로 로그인) 모듈 초기화
     */
    private void setNaver(){
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(this,CLIENT_ID,CLIENT_SECRET,CLIENT_NAME);
        mOAuthLoginModule.logout(this);
    }

    private class RequestApiTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            String url = "https://openapi.naver.com/v1/nid/getUserProfile.xml";
            String at = mOAuthLoginModule.getAccessToken(context);
            mNaverUserInfoMap = requestNaverUserInfo(mOAuthLoginModule.requestApi(context,at,url));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                if(mNaverUserInfoMap.get("resultcode").equals("00")){

                    name = mNaverUserInfoMap.get("name");
                    email = mNaverUserInfoMap.get("email");
                    age = mNaverUserInfoMap.get("age");
                    if(age.startsWith("10")){
                        age = "0";
                    }else if(age.startsWith("20")){
                        age = "1";
                    }else if(age.startsWith("30")){
                        age = "2";
                    }else if(age.startsWith("40")){
                        age = "3";
                    }else{
                        age = "4";
                    }
                    gender = mNaverUserInfoMap.get("gender");
                    if(gender.equals("M")){
                        gender = "0";
                    }else{
                        gender = "1";
                    }

                    // name, email, password, age, gender
                    int resultCode = new JoinTask().execute(name,email,"none",age,gender).get();

                    switch (resultCode){
                        case 1000:
                        case 2000:
                            SharedPreferences prefs = getSharedPreferences("login",MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email",email);
                            editor.putString("name",name);
                            editor.apply();
                            startActivity(new Intent(SelectActivity.this,MainActivity.class));
                            finish();
                            break;
                        case 4000:
                            Toast.makeText(getApplicationContext(),"네이버 로그인 진행 중 에러가 발생하였습니다.",Toast.LENGTH_SHORT).show();
                            break;
                    }
//
//                    ConnectionListJava getResponse = RetrofitConnJava.getRetrofit().create(ConnectionListJava.class);
//
//                    // map 에 회원가입 필수 입력 사항 (name, email, password) 을 넣는다.
//                    HashMap<String, RequestBody> map = new HashMap();
//                    map.put("name",RequestBody.create(MediaType.parse("multipart/form-data"),name));
//                    map.put("email",RequestBody.create(MediaType.parse("multipart/form-data"),email));
//                    map.put("password",RequestBody.create(MediaType.parse("multipart/form-data"),"none"));
//                    map.put("age",RequestBody.create(MediaType.parse("multipart/form-data"),age));
//                    map.put("gender",RequestBody.create(MediaType.parse("multipart/form-data"),gender));
//
//                    // 서버에 map 에 넣은 회원 정보를 토대로 회원 가입을 요청한다.
//                    // 회원가입 요청이 정상적으로 전달 되고 join.php 을 읽는데 오류가 없으면 response 는 success 이다.
//                    // 회원가입 요청시 이미 존재하는 이메일로 회원 가입 요청을 한다면 response.body().message 에서 -1 을 전달한다.
//                    // 회원가입이 정상적으로 이루어지면 userNo 가 전달되고, 전달된 userNo 와 email 을 SharedPreferences 에 저장한다.
//
//                    Call<JoinResponseJava> call = getResponse.requestJoin(map);
//                    call.enqueue(new Callback<JoinResponseJava>() {
//                        @Override
//                        public void onResponse(Call<JoinResponseJava> call, Response<JoinResponseJava> response) {
//                            boolean result = response.body().getSuccess();
//                            int userNo = response.body().getMessage();
//                            if (result){
//                                SharedPreferences prefs = getSharedPreferences("login",MODE_PRIVATE);
//                                SharedPreferences.Editor editor = prefs.edit();
//                                if(userNo == -1){
//                                    editor.putString("email",email);
//                                    editor.putString("name",name);
//                                    editor.apply();
//                                }else{
//                                    editor.putInt("userNo",userNo);
//                                    editor.putString("email",email);
//                                    editor.putString("name",name);
//                                }
//                                editor.apply();
//                                startActivity(new Intent(SelectActivity.this,MainActivity.class));
//                                finish();
//                            }else {
//                                Log.e("result","failed");
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<JoinResponseJava> call, Throwable t) {
//                            Log.e("onFailure",t.toString());
//                        }
//                    });
                }else{
                    Log.e("Naver info","failed load information");
                    Toast.makeText(getApplicationContext(),"네이버 로그인 진행 중 에러가 발생하였습니다.",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private Map<String,String> requestNaverUserInfo(String data){
        System.out.println(data);
        String f_array[] = new String[7];

        try{
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFactory.newPullParser();
            InputStream input = new ByteArrayInputStream(data.getBytes("UTF-8"));
            parser.setInput(input,"UTF-8");

            int parserEvent = parser.getEventType();
            String tag;
            boolean inText = false;

            int colIdx = 0;



            while (parserEvent != XmlPullParser.END_DOCUMENT){
                switch (parserEvent){
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if(tag.compareTo("xml")==0){
                            inText = false;
                        } else if (tag.compareTo("data")==0){
                            inText = false;
                        } else if (tag.compareTo("result")==0){
                            inText = false;
                        }else if (tag.compareTo("resultCode")==0){
                            inText = false;
                        }else if (tag.compareTo("message")==0){
                            inText = false;
                        }else if (tag.compareTo("response")==0){
                            inText = false;
                        }else{
                            inText = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(inText){
                            if(parser.getText() == null){
                                f_array[colIdx] = "";
                            } else {
                                f_array[colIdx] = parser.getText().trim();
                            }

                            colIdx++;
                        }
                        inText = false;
                        break;
                    case XmlPullParser.END_TAG:
                        inText = false;
                        break;
                }

                parserEvent = parser.next();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("resultcode",f_array[0]);
        resultMap.put("message",f_array[1]);
        resultMap.put("profile_image",f_array[2]);
        resultMap.put("age",f_array[3]);
        resultMap.put("gender",f_array[4]);
        resultMap.put("email",f_array[5]);
        resultMap.put("name",f_array[6]);
        Log.d("resultcode",f_array[0]);
        return resultMap;
    }

    /**
     * SelectActivity 배경에서 재생되는 동영상을 설정함
     */
    private void setBackgroundVideo(){
        TextureVideoView videoClip = findViewById(R.id.videoClip);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+ "/"+R.raw.intro_tlive);
        videoClip.setVideoURI(uri);
        videoClip.start();
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - mLastTimeBackPressed < 1500){
            finish();
            return;
        }
        mLastTimeBackPressed = System.currentTimeMillis();
        Toast.makeText(getApplicationContext(),"한 번 더 누르면 종료됩니다",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        context = null;
        super.onDestroy();
    }
}
