package com.testexam.charlie.tlive.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.testexam.charlie.tlive.main.follow.chat.Chat;
import com.testexam.charlie.tlive.main.follow.chat.Room;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DBHelper extends SQLiteOpenHelper{

    private Context context;
    private SQLiteDatabase db = null;

    // 현재 시간을 구하기 위해 사용하는 변수들
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /*
    채팅방 (chatRooms) 과 채팅 내역 (chatList) 에서 사용되는 변수

    @variable : senderEmail - 채팅 메시지를 보낸 사람의 이메일
    @variable : senderName - 채팅 메시지를 보낸 사람의 이름
    @variable : currentChat - 가장 최근 보낸 메시지, 채팅방 목록에서 최근 보낸 메시지를 표시할 때 사용된다.
    @variable : currentTime - 가장 최근에 메시지를 받은 시간, 채팅방 목록에서 최근 메시지를 받은 시간을 표시할 때 사용된다.
    @variable : receiveMsg - 받은 메시지
     */
    private String targetEmail;
    private String senderEmail;
    private String senderName;
    private String currentChat;
    private String currentTime;
    private String receiveMsg;
    private boolean isReceive;

    /*
    현재 로그인한 사용자의 정보를 저장하는 변수
    @variable : mEmail - 사용자의 이메일
    @variable : mName - 사용자의 이름
     */
    private String mEmail;
    private String mName;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sb;

        /*
        채팅방 DB 생성
        no : 채팅방의 인덱스 번호
        targetEmail : 상대방 이메일
        targetName : 상대방 이름
        currentChat : 최근 받은 메시지
        currentTime : 최근 메시지 받은 시간
        badgeCount : 읽지 않은 메시지의 개수
         */
        sb = new StringBuffer();
        sb.append("CREATE TABLE chatRooms (");
        sb.append("no INTEGER PRIMARY KEY AUTOINCREMENT,");
        sb.append("targetEmail TEXT,");
        sb.append("targetName TEXT,");
        sb.append("currentChat TEXT,");
        sb.append("currentTime TEXT,");
        sb.append("badgeCount INTEGER DEFAULT 0 )");
        db.execSQL(sb.toString());
        Log.e("채팅 방 테이블","생성 완료");

        /*
        채팅 내역 DB 생성

        no : 채팅 내역의 인덱스 번호
        senderEmail : 메시지를 보낸 사람의 이메일
        senderName : 메시지를 보낸 사람의 이름
        msg : 메시지의 내용
         */
        sb = new StringBuffer();
        sb.append("CREATE TABLE chatList (");
        sb.append("no INTEGER PRIMARY KEY AUTOINCREMENT,");
        sb.append("targetEmail TEXT,");
        sb.append("senderEmail TEXT,");
        sb.append("senderName TEXT,");
        sb.append("msg TEXT )");
        db.execSQL(sb.toString());
        Log.e("채팅 내역 테이블","생성 완료");

        SharedPreferences sp = context.getSharedPreferences("login",Context.MODE_PRIVATE);
        mEmail = sp.getString("email",null);
        mName = sp.getString("name",null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("채팅 테이블","버전 업그레이드");
    }

    public void chatDB(){
        if(db == null){
            db = getWritableDatabase();
        }
    }

    /**
     * 채팅 메시지가 소켓을 통해 전달 되었고, 그 메시지를 DB 에 저장할 때 호출하는 메소드.
     *
     * 1. 들어온 메시지를 JSON 형식으로 파싱한다.
     * 2. 같은 targetEmail 를 가지고 있는 채팅방이 있는지 확인한다.
     * 3. 같은 targetEmail 를 가진 채팅방이 있다면 그 채팅방의 최근 받은 메시지 등 상태를 업데이트 하고 , 아니라면 새로운 채팅방을 생성한다.
     * 4. 채팅 내역에 메시지를 저장한다.
     * @param msg : 소켓으로 부터 받은 메시지
     */
    public void receiveChat(String msg){
        Log.e("receiveChat", msg);
        try {
            //  1. 들어온 메시지를 JSON 형식으로 파싱한다.
            JSONObject msgObject = new JSONObject(msg);

            targetEmail = msgObject.getString("targetEmail");
            senderEmail = msgObject.getString("senderEmail");
            senderName = msgObject.getString("senderName");
            currentChat = msgObject.getString("msg");

            isReceive = msgObject.getBoolean("isReceive");

            Date mDate = new Date(System.currentTimeMillis());
            currentTime = mFormat.format(mDate);

            receiveMsg = msgObject.getString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //  2. 같은 roomId 를 가지고 있는 채팅방이 있는지 확인한다.
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT no FROM chatRooms where targetEmail = ?");
        SQLiteDatabase db = getReadableDatabase();

        // 내가 받은 메시지라면 보낸 사람의 이메일을
        // 내가 보낸 메시지라면 받을 사람의 이메일로 채팅 방을 찾는다.
        Cursor cursor;
        if(isReceive){
            cursor = db.rawQuery(sb.toString(),new String[]{senderEmail});
        }else{
            cursor = db.rawQuery(sb.toString(),new String[]{targetEmail});
        }
        // 3. 같은 roomId 를 가진 채팅방이 있다면 그 채팅방의 최근 받은 메시지 등 상태를 업데이트 하고 , 아니라면 새로운 채팅방을 생성한다.
        if(cursor.getCount()==0){ // 기존에 방이 없는 경우.
            // 기존에 존재하지 않은 방이므로 새로운 방을 개설한다.
            sb = new StringBuffer();

            sb.append("INSERT INTO chatRooms (targetEmail, targetName, currentChat, currentTime, badgeCount ) ");
            sb.append("VALUES (?, ?, ?, ?, ?)");
            Object[] params;

            // 내가 받은 메시지로 새로운 채팅방을 만들어야 하는 경우 타겟 이메일을 보낸 사람으로
            // 내가 보낸 메시지로 새로운 채팅방을 만들어야 하는 경우 타겟 이메일을 받을 사람으로 설정한다.
            if(isReceive){
                params = new Object[]{
                        senderEmail,
                        senderName,
                        currentChat,
                        currentTime,
                        1
                };
            }else{
                params = new Object[]{
                        targetEmail,
                        senderName,
                        currentChat,
                        currentTime,
                        1
                };
            }

            db.execSQL(sb.toString(),params); // 데이터베이스에 새로운 채팅방을 삽입한다.

        }else {
            // 기존에 나의 DB 에 존재하던 채팅방
            // 최근 받은 메시지와 시간을 업데이트한다.
            sb = new StringBuffer();
            sb.append("UPDATE chatRooms SET ");
            sb.append("currentChat = ? ,");
            sb.append("currentTime = ? ,");
            sb.append("badgeCount = badgeCount + 1 ");
            sb.append("WHERE targetEmail = ?");

            // 내가 받은 메시지라면 보낸 사람으로 검색한다.
            if(isReceive){
                db.execSQL(sb.toString(), new Object[]{
                        currentChat,
                        currentTime,
                        senderEmail
                });

            // 내가 보낸 메시지라면 받을 사람으로 검색한다.
            }else{
                db.execSQL(sb.toString(), new Object[]{
                        currentChat,
                        currentTime,
                        targetEmail
                });
            }


        }

        // 4. 채팅 내역에 메시지를 저장한다.
        sb = new StringBuffer();
        sb.append("INSERT INTO chatList (targetEmail,senderEmail, senderName, msg) ");
        sb.append("VALUES (?,?,?,?) ");
        if(isReceive){ // 소켓을 통해 받은 메시지라면 , 타겟은 senderEmail
            db.execSQL(sb.toString(),new Object[]{
                    senderEmail,
                    senderEmail,
                    senderName,
                    receiveMsg
            });
        }else{ // 내가 보낸 메시지라면 타겟은 targetEmail
            db.execSQL(sb.toString(),new Object[]{
                    targetEmail,
                    senderEmail,
                    senderName,
                    receiveMsg
            });
        }



        if(!cursor.isClosed()){
            cursor.close();
        }
    }

    /**
     * 채팅 방 목록을 불러온다.
     * @return 채팅방 목록
     */
    public ArrayList<Room> getChatRoomList(){
        ArrayList<Room> roomItems = new ArrayList<>();
        String sb = "SELECT targetEmail, targetName, currentChat, badgeCount FROM chatRooms " +
                "Order by datetime(currentTime) desc";

        Cursor cursor = db.rawQuery(sb,null);
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            //String roodId,String name, int size, String content, String currentTime
            roomItems.add(new Room(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3)
            ));
        }
        cursor.close();
        return roomItems;
    }

    /**
     * 채팅 내역을 불러온다
     * @param targetEmail : 불러올 채팅 상대방의 이메일
     * @return 채팅 내역
     */
    public ArrayList<Chat> getChatList(String targetEmail) {

        ArrayList<Chat> chatArray = new ArrayList<>();
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT senderName, senderEmail, msg FROM chatList WHERE targetEmail = ?");
        Cursor cursor = db.rawQuery(sb.toString(),new String[]{targetEmail});
        if(cursor.getCount() != 0){
            try {
                cursor.moveToFirst();
                for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                    chatArray.add(new Chat(
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2)
                    ));
                }
            } finally {
                cursor.close();
            }
        }else{
            Log.d("getChatList","cursor count 0");
            return new ArrayList<>();
        }
        if(!cursor.isClosed()){
            cursor.close();
        }
        sb = new StringBuffer();
        sb.append("UPDATE chatRooms SET badgeCount = 0 WHERE targetEmail = ? ");
        db.execSQL(sb.toString(),new String[]{targetEmail});
        Log.d("getChatList","cursor count "+chatArray.size());
        return chatArray;
    }

    public int getRoomNum(String senderEmail) {
        db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 'no' FROM chatRooms WHERE targetEmail = ?",new String[]{senderEmail});
        int roomNum;
        if(cursor.moveToNext()){
            roomNum = cursor.getInt(0);
        }else{
            roomNum = -1;
        }
        cursor.close();
        return roomNum;
    }
}
