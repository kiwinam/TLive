package com.testexam.charlie.tlive.retrofit_java;

import com.google.gson.annotations.SerializedName;

/**
 * Created by charlie on 2018. 5. 23..
 */

public class JoinResponseJava {

    @SerializedName("success")
    boolean success;
    @SerializedName("userNo")
    int userNo;

    public int getMessage(){
        return userNo;
    }

    public boolean getSuccess(){
        return success;
    }

}