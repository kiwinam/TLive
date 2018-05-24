package com.testexam.charlie.tlive.retrofit_java;

import com.google.gson.annotations.SerializedName;

/**
 * 로그인 진행 중 ,레트로핏 응답 JSON 데이터 클래스
 *
 * Created by charlie on 2018. 5. 24
 */

public class LoginResponse {
    @SerializedName("success")
    boolean success;

    @SerializedName("userNo")
    int userNo;

    @SerializedName("name")
    String name;

    @SerializedName("gender")
    int gender;

    @SerializedName("age")
    int age;

    @SerializedName("profileUrl")
    String profileUrl;

    public boolean getSuccess(){
        return success;
    }

    public int getUserNo(){
        return userNo;
    }

    public String getName(){
        return name;
    }

    public int getGender(){
        return gender;
    }

    public int getAge(){
        return age;
    }

    public String getProfileUrl(){
        return profileUrl;
    }
}
