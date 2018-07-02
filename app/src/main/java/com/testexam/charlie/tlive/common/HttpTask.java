package com.testexam.charlie.tlive.common;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Http 요청을 처리하는 메소드
 * Http Connection 으로 서버에 http 요청을 전달한다.
 * http 요청이 처리가 완료되면 result 값을 반환한다.
 *
 * Created by charlie on 2018. 5. 30
 */
public class HttpTask extends AsyncTask<String, Void, String> {
    private String requestUrl; // 요청할 서버 URL
    private ArrayList<Params> params; // 요청시 서버에 전달할 파라미터를 가지고 있는 ArrayList

    private String result;  // http response body 값을 저장하는 변수
    private HttpURLConnection conn;

    /*
     * Http Constructor, requestUrl 과 params 변수를 초기화한다.
     */
    public HttpTask(String requestUrl, ArrayList<Params> params){
        this.requestUrl = requestUrl;   // 요청 URL 초기화
        this.params = params;           // 파라미터 초기화
    }

    /*
     * 서버 URL 로 HttpConnection 을 생성한다.
     *
     * 1. URL Connection 을 생성한다.
     * 2. 요청시 같이 보내야할 파라미터를 넣는다.
     * 3. 결과 값을 받는다
     * 4. HttpConnection 을 Disconnect 한다.
     */
    @Override
    protected String doInBackground(String... strings) {
        try{
            // 1. URL Connection 을 생성한다.
            URL url = new URL(getRequestUrl()); // getRequestUrl 메소드를 호출하여 연결할 URL 을 가져온다.
            conn = (HttpURLConnection) url.openConnection();    // HttpUrlConnection 생성
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");   // Content type 설정
            conn.setRequestMethod("POST");  // 파라미터 전송 방식 POST 로 설정

            // 2. 요청시 같이 보내야할 파라미터를 넣는다.
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(getParameters()); //  HttpUrlConnection OutputStream 에 파라미터를 넣는다.
            osw.flush();    // OutputStreamWriter 를 비운다.

            // 3. 결과 값을 받는다
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
                result = buffer.toString(); // 버퍼에 있는 값을 result 로 옮긴다 .
            } else{
                result = "";    // 요청 응답 코드가 200이 아닌경우 result 에 공백을 넣는다.
                Timber.tag("HttpTask").e("Http response : %s", conn.getResponseCode()); // Http 응답 코드가 정상이 아닐 경우 로그를 출력한다.
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally {
            // 4. HttpConnection 을 Disconnect 한다.
            conn.disconnect();
        }
        return result;  // 결과를 반환한다.
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
        boolean isFirst = true; // 첫 번째로 들어가는 파라미터를 검사하는 변수

        for(Params param : params){
            // 첫 번째 파라미터 앞에 & 를 붙이지 않기 위해 isFirst 값을 true 에서 false 로 변경한다.
            if(isFirst) { isFirst = false; }
            else{ paramResult.append("&"); }    // 두 번째 파라미터부터 & 를 추가한다.

            String encoderType = "UTF-8";   //
            paramResult.append(URLEncoder.encode(param.getKey(), encoderType));
            paramResult.append("=");
            paramResult.append(URLEncoder.encode(param.getValue(), encoderType));
        }
        return paramResult.toString();
    }
}
