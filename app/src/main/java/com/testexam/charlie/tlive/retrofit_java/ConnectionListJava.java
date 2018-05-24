package com.testexam.charlie.tlive.retrofit_java;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

/**
 * Created by charlie on 2018. 5. 23
 */

public interface ConnectionListJava {

    @Multipart
    @POST("join.php")
    Call<JoinResponseJava> requestJoin(@PartMap()Map<String, RequestBody> partMap);

    @Multipart
    @POST("UpdateOptionalInfo.php")
    Call<ResultResponse> requestUpdateInfo(@PartMap()Map<String, RequestBody> partMap);

    @Multipart
    @POST("login.php")
    Call<LoginResponse> requestLogin(@PartMap()Map<String, RequestBody> partMap);
}
