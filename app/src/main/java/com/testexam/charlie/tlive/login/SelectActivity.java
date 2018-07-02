package com.testexam.charlie.tlive.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.crust87.texturevideoview.widget.TextureVideoView;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.testexam.charlie.tlive.common.BaseActivity;
import com.testexam.charlie.tlive.common.HttpTask;
import com.testexam.charlie.tlive.common.Params;
import com.testexam.charlie.tlive.main.MainActivity;
import com.testexam.charlie.tlive.R;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;


/**
 * 이메일 로그인과 네이버 로그인을 선택하는 Activity
 *
 * 이메일 로그인 버튼을 누르면 LoginActivity 로 이동하고,
 * 네이버 로그인 버튼을 누르면 네이버 아이디로 로그인 API 를 호출하여 로그인을 시도한다.
 *
 * Created by charlie on 2018. 5. 22
 */

public class SelectActivity extends BaseActivity {
    private final static String CLIENT_NAME = "T Live";     // 네아로에서 등록한 Cline name
    public Map<String, String> mNaverUserInfoMap;           // 네이버 로그인 결과가 저장되는 Map 변수
    public static OAuthLogin mOAuthLoginModule;             // 네이버 로그인 모듈 객체

    private Context context;
    private long mLastTimeBackPressed;      // 마지막으로 뒤로가기 버튼을 누른 시점을 저장하는 변수

    // 네이버 인증을 시도하는 핸들러
    // 네이버 로그인 모듈의 초기화가 성공하면 네이버 인증을 진행하는 RequestApiTask 클래스를 호출한다.
    @SuppressLint("HandlerLeak")
    public OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() { // 네이버 로그인 모듈 객체를 초기화한다.
        @Override
        public void run(boolean success) {
            if(success){    // 로그인 모듈 객체 초기화가 성공한 경우
                new RequestApiTask().execute(); // 네이버 로그인을 요청을 시작한다.
            }else{  // 로그인 모듈 객체 초기화가 실패한 경우
                String errorCode = mOAuthLoginModule.getLastErrorCode(context).getCode();
                String errorDesc = mOAuthLoginModule.getLastErrorDesc(context);
                Toast.makeText(context, "errorCode:"+errorCode+", errordesc:"+errorDesc,Toast.LENGTH_SHORT).show(); // 모듈 실패의 원인을 토스트로 보여준다.
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        context = this; // context 객체에 현재 activity 의 context 를 넣는다.

        setBackgroundVideo();   // 배경에서 자동 재생되는 동영상을 설정한다.
        setNaver();     // 네이버 로그인 모듈을 초기화한다.

        Button emailBtn = findViewById(R.id.selectEmailBtn);    // 이메일 로그인 버튼 객체 초기화
        Button naverBtn = findViewById(R.id.selectNaverBtn);    // 네이버 로그인 버튼 객체 초기화

        emailBtn.setOnClickListener(v -> {  // 이메일 로그인 버튼을 누르면 LoginActivity 로 이동한다.
            startActivity(new Intent(getApplicationContext(),LoginActivity.class)); // LoginActivity 를 시작한다.
            finish();   // 현재 액티비티를 종료한다.
        });
        // 네이버 로그인 버튼을 누르면 네이버 로그인을 시도한다.
        naverBtn.setOnClickListener(v -> mOAuthLoginModule.startOauthLoginActivity(SelectActivity.this,mOAuthLoginHandler));
    }

    /*
     * 네아로 (네이버 아이디로 로그인) 모듈 초기화
     */
    private void setNaver(){
        mOAuthLoginModule = OAuthLogin.getInstance();   // 로그인 모듈 인스턴스 생성
        mOAuthLoginModule.init(this,getString(R.string.naver_client_id),getString(R.string.naver_client_secret),CLIENT_NAME); // 네이버 클라이언트 id 와 키로 초기화 시도
        mOAuthLoginModule.logout(this);
    }

    /**
     * 네이버 로그인 인증을 요청하는 클래스
     *
     * AsyncTask 를 상속받아 doInBackground 에서 인증 요청을 실행한다.
     * 인증 결과 코드에 따라 로그인 진행 여부를 결정한다.
     * 인증 코드가 00 (인증 성공) 일 경우 유저 정보가 들어 있는 맵에서 데이터를 가쟈와 로그인을 진행한다.
     * 인증 코드가 00 이 아닐 경우 로그인이 실패했다는 메시지를 띄워준다.
     */
    @SuppressLint("StaticFieldLeak")
    private class RequestApiTask extends AsyncTask<Void,Void,Void>{
        // 네이버 아이디로 로그인하기 api url 로 인증 요청을 한다.
        @Override
        protected Void doInBackground(Void... voids) {
            String url = "https://openapi.naver.com/v1/nid/getUserProfile.xml"; // 네아로 api url
            String at = mOAuthLoginModule.getAccessToken(context);      // 네아로 모듈에서 Access token 을 가져온다.
            // requestNaverUserInfo() 메소드를 호출하여 네이버 아이디 로그인을 진행한다.
            // 결과 값은 mNaverUserInfoMap 에 저장된다.
            mNaverUserInfoMap = requestNaverUserInfo(mOAuthLoginModule.requestApi(context,at,url));
            return null;
        }

        // 네아로 프로세스가 끝난 후 결과에 따라 로그인 진행 여부를 결정한다.
        // 인증이 성공한 경우 유저 정보를 파라미터에 담아 서버에 회원가입 요청을 한다.
        // 인증이 실패한 경우 인증이 실패했다는 메시지를 띄워준다.
        @Override
        protected void onPostExecute(Void aVoid) {
            try{
                // 네이버 로그인 인증 결과가 00 (인증성공) 인 경우
                if(mNaverUserInfoMap.get("resultcode").equals("00")){

                    String name = mNaverUserInfoMap.get("name");    // 이름을 가져온다
                    String email = mNaverUserInfoMap.get("email");  // 이메일을 가져온다.
                    String age = mNaverUserInfoMap.get("age");      // 연령대를 가져온다 . 저장되어 있는 연령대는 10대, 20대 ... 등으로 시작하는 숫자에 따라 age 값을 저장한다.
                    if(age.startsWith("10")){   // 10 대
                        age = "0";
                    }else if(age.startsWith("20")){ // 20 대
                        age = "1";
                    }else if(age.startsWith("30")){ // 30 대
                        age = "2";
                    }else if(age.startsWith("40")){ // 40 대
                        age = "3";
                    }else{      // 그 이상 나이대는 50대 이상으로 처리한다.
                        age = "4";
                    }
                    String gender = mNaverUserInfoMap.get("gender");    // 성별을 가져온다. 성별은 M, W 로 구분된다.
                    if(gender.equals("M")){ // 남자일 경우 0
                        gender = "0";
                    }else{      // 여자일 경우 1
                        gender = "1";
                    }

                    // 파라미터에 회원 정보를 담는다.
                    ArrayList<Params> paramList = new ArrayList<>(); // Params 객체를 담을 ArrayList 초기화
                    paramList.add(new Params("name", name));    // 파라미터에 name 추가
                    paramList.add(new Params("email", email));  // 파라미터에 email 추가
                    paramList.add(new Params("password","none"));   // 파라미터에 password 추가
                    paramList.add(new Params("age", age));  // 파라미터에 age 추가
                    paramList.add(new Params("gender",gender)); // 파라미터에 gender 추가

                    String result = new HttpTask("join.php",paramList).execute().get(); // 서버 join.php 에 회원 가입 요청을 한다. 리턴되는 결과는 result 변수에 담는다.
                    JSONObject resultObject = new JSONObject(result);   // 리턴된 result 값이 JSONObject 형태이기 때문에 JSONObject 로 파싱한다.
                    if(resultObject.getBoolean("success")){ // 회원 가입이 성공했다면
                        SharedPreferences prefs = getSharedPreferences("login",MODE_PRIVATE);   // login 이름으로 된 SharedPreference 에서 회원 정보를 가져온다.
                        SharedPreferences.Editor editor = prefs.edit(); // SharedPreference 를 수정할 수 있도록 Editor 객체를 선언한다.
                        editor.putString("email", email);   // editor 에 email 를 추가한다.
                        editor.putString("name", name);     // editor 에 name 를 추가한다.
                        editor.apply();     // Editor 에 변경된 사항을 승인하여 SharedPreference 에 저장한다.
                        startActivity(new Intent(SelectActivity.this,MainActivity.class));  // MainActivity 로 이동한다.
                        finish();   // 현재 액티비티를 종료한다.
                    }else{  // 회원 가입이 실패한 경우 토스트 메시지를 띄워준다
                        Toast.makeText(getApplicationContext(),"네이버 로그인 진행 중 에러가 발생하였습니다.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Timber.tag("Naver info").e("failed load information");
                    Toast.makeText(getApplicationContext(),"네이버 로그인 진행 중 에러가 발생하였습니다.",Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /*
     * 네아로 모듈에서 얻은 네이버 유저 데이터를 파싱하여 맵에 담는 메소드
     */
    private Map<String,String> requestNaverUserInfo(String data){
        String f_array[] = new String[7];   // 유저 정보를 담을 String 배열

        try{
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();    // Xml 파서 팩토리를 생성한다.
            XmlPullParser parser = parserFactory.newPullParser();  // xml 파서를 팩토리에서 새로 가져온다.
            InputStream input = new ByteArrayInputStream(data.getBytes("UTF-8"));
            parser.setInput(input,"UTF-8"); // 파서의 인코딩 타입을 UTF-8 로 설정한다.

            int parserEvent = parser.getEventType();    // 파서의 이벤트 타입을 가져온다.
            String tag;
            boolean inText = false;

            int colIdx = 0;

            // 파서의 이벤트가 문서의 끝일 때까지 -> 문서를 끝까지 다 읽는다.
            while (parserEvent != XmlPullParser.END_DOCUMENT){
                switch (parserEvent) {
                    // 파서 이벤트가 태그의 시작이라면
                    case XmlPullParser.START_TAG:
                        tag = parser.getName(); // 태그의 이름을 가져온다.
                        inText = tag.compareTo("xml") != 0
                                && tag.compareTo("data") != 0
                                && tag.compareTo("result") != 0
                                && tag.compareTo("resultCode") != 0
                                && tag.compareTo("message") != 0 &&
                                tag.compareTo("response") != 0;
                        break;
                    // 파서 이벤트가 TEXT 라면
                    case XmlPullParser.TEXT:
                        if (inText) {
                            if (parser.getText() == null) {
                                f_array[colIdx] = "";
                            } else {
                                f_array[colIdx] = parser.getText().trim(); // 텍스트를 가져와 저장한다.
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

        // 결과 배열에 네이버 유저 정보를 넣는다.
        Map<String, String> resultMap = new HashMap<>();    // 유저 정보를 담을 맵
        resultMap.put("resultcode",f_array[0]);     // 결과 코드
        resultMap.put("message",f_array[1]);        // 메세지
        resultMap.put("profile_image",f_array[2]);  // 프로필 사진 경로
        resultMap.put("age",f_array[3]);            // 연령대
        resultMap.put("gender",f_array[4]);         // 성별
        resultMap.put("email",f_array[5]);          // 이메일
        resultMap.put("name",f_array[6]);           // 이름
        Timber.tag("resultcode").d(f_array[0]);
        return resultMap;       // 결과를 리턴한다.
    }

    /*
     * SelectActivity 배경에서 재생되는 동영상을 설정함
     */
    private void setBackgroundVideo(){
        TextureVideoView videoClip = findViewById(R.id.videoClip);
        Uri uri = Uri.parse("android.resource://"+getPackageName()+ "/"+R.raw.intro_tlive);
        videoClip.setVideoURI(uri);
        videoClip.start();
    }

    /*
     * 뒤로 가기 버튼을 1.5 초 내에 두 번 누르면 액티비티를 종료한다.
     *
     * 사용자가 실수로 뒤로가기 버튼을 클릭했을 때 액티비티가 종료되는 것을 방지한다.
     * 뒤로가기 버튼을 누른 시점을 mLastTimeBackPressed 에 저장한다.
     * 다음 뒤로가기 버튼을 누른 시점이 mLastTimeBackPressed 와 1.5 초 미만으로 차이나면 Activity 를 종료한다.
     */
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - mLastTimeBackPressed < 1500){
            finish();
            return;
        }
        mLastTimeBackPressed = System.currentTimeMillis();
        Toast.makeText(getApplicationContext(),"한 번 더 누르면 종료됩니다",Toast.LENGTH_SHORT).show();
    }

    // Activity 를 onDestroy 하기 전에 context 변수를 null 한다.
    @Override
    protected void onDestroy() {
        context = null;
        super.onDestroy();
    }
}
