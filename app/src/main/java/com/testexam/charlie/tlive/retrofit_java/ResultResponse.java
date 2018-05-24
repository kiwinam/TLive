package com.testexam.charlie.tlive.retrofit_java;

import com.google.gson.annotations.SerializedName;

/**
 * Created by charlie on 2018. 5. 24
 */

public class ResultResponse {
    @SerializedName("success")
    boolean success;
    public boolean getSuccess(){
        return success;
    }
}
