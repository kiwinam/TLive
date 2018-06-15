package com.testexam.charlie.tlive.common;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Http 요청을 처리하는 메소드
 * Http Connection 으로 서버에 http 요청을 전달한다.
 * http 요청이 처리가 완료되면 result 값을 반환한다.
 *
 * Created by charlie on 2018. 5. 30
 */
public class HttpTask extends AsyncTask<String, Void, String> {
    //private final String serverUrl = "http://13.209.41.194:80/app/";
    private String requestUrl; // 요청할 서버 URL
    private ArrayList<Params> params; // 요청시 전달할 파라미터

    private String result;  // http response body 값을 저장하는 변수
    private HttpURLConnection conn;
    /*
     * Http Constructor, requestUrl 과 params 변수를 초기화한다.
     */
    public HttpTask(String requestUrl, ArrayList<Params> params){
        this.requestUrl = requestUrl;
        this.params = params;
    }

    /*
     *
     */
    @Override
    protected String doInBackground(String... strings) {
        try{
            URL url = new URL(getRequestUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");

            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(getParameters()); // 파라미터를 넣는다.
            osw.flush();

            // Http 응답 코드가 HTTP_OK (200) 일 경우 전달된 결과값을 result 변수에 저장 후 리턴한다.
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream());
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder buffer = new StringBuilder();

                String tempStr; // response 의 값을 임시 저장해두는 String 값

                // response 의 값을 더이상 읽을게 없을 때 까지 읽고, 읽은 값을 tempStr 에 저장한다.
                while((tempStr = reader.readLine()) != null ){
                    buffer.append(tempStr);
                }
                result = buffer.toString();
            } else{
                result = "";
                Log.e("HttpTask","Http response : "+conn.getResponseCode()); // Http 응답 코드가 정상이 아닐 경우 로그를 출력한다.
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        return result;
    }

    /*
     * requestUrl 를 반환한다.
     */
    private String getRequestUrl() {
        String serverUrl = "http://13.125.64.135:80/app/";
        return serverUrl +requestUrl; }

    /*
     * 전달 받은 파라미터 리스트를 Key=Value 형식의 String 으로 반환한다.
     */
    private String getParameters() throws UnsupportedEncodingException{
        StringBuilder paramResult = new StringBuilder();
        boolean isFirst = true;
        for(Params param : params){
            if(isFirst) { isFirst = false; }
            else{ paramResult.append("&"); }

            String encoderType = "UTF-8";
            paramResult.append(URLEncoder.encode(param.getKey(), encoderType));
            paramResult.append("=");
            paramResult.append(URLEncoder.encode(param.getValue(), encoderType));
        }
        return paramResult.toString();
    }
}
