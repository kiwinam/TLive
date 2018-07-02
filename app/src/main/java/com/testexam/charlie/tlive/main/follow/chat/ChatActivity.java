package com.testexam.charlie.tlive.main.follow.chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.common.BaseActivity;
import com.testexam.charlie.tlive.common.DBHelper;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Netty 를 이용하여 친구와 1:1 대화를 하는 Activity
 *
 * ChatService 에 등록되어 있는 소켓을 통해 서버와 연결된다.
 * 네티 소켓을 통해 채팅 메시지를 주고 받는다.
 * 주고 받는 메시지들은 SQLite 를 통해 디바이스 내부에 저장된다.
 * 기존에 있는 채팅방에 들어가게 되면 SQLite 에서 이전 채팅 내역을 불러와 chatRv 에 표시한다.
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener{
    private RecyclerView chatRv;    // 채팅 메시지를 보여주는 RecyclerView
    private EditText chatSendEt;    // 채팅 메시지를 입력하는 EditText
    private Button chatSendBtn;     // 채팅 보내기 버튼

    private ArrayList<Chat> chatList;   // 채팅 내역을 가지고 있는 ArrayList
    private ChatAdapter chatAdapter;    // chatRv 의 어댑터

    private ChatService chatService;    // 채팅 서비스 객체

    private boolean isBound;    // 채팅 서비스와 바인드 유무를 판단하는 변수

    private String myEmail; // 로그인한 유저의 이메일

    private String mTargetEmail; // 채팅 상대방 이메일
    private String mTargetName; // 채팅 상대방 이름

    private DBHelper dbHelper;  // 디바이스에 SQLite 를 이용하여 채팅 내역을 저장할 때 사용하는 DBHelper 객체

    /**
     * 채팅 서비스와 바인딩.
     * ChatService 와 바인딩한다. 바인딩 할 때 콜백 메소드를 등록한다.
     */
    private ServiceConnection chatConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {   // 서비스에 바인딩 된 경우
            ChatService.ChatBinder chatBinder = (ChatService.ChatBinder) service;
            chatService = chatBinder.getService();  // 채팅 서비스 객체를 가져온다.
            chatService.registerCallback(mCallback);    // 채팅 서비스에 등록해야하는 콜백 메서드를 등록한다.
            isBound = true;     // 바인드 유무를 판단하는 변수의 값을 true 로 변경한다.
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { // 서비스에 바인딩 되지 않은 경우
            isBound = false;    // 바인드 유무를 판단하는 변수의 값을 false 로 변경한다.
        }
    };

    /*
     * 채팅 서비스에 등록해야하는 ReceiveCallback
     */
    ChatService.ReceiveCallback mCallback = new ChatService.ReceiveCallback() {

        @Override
        public void receiveMsg(String msg) {    // ChatService 소켓에서 메시지가 도착한 경우
            Message message = readHandler.obtainMessage();  // 새로운 채팅을 SQLite 에서 읽어오는 핸들러를 실행한다.
            readHandler.sendMessage(message);
        }

        // 소켓으로 새로운 메시지가 전달됐을 때, 현재 보고 있는 Activity 에 targetEmail 과 일치하면
        // 알림을 보여주지 않는다.
        // 현재 targetEmail 과 새로온 메시지의 senderEmail 이 일치하면 false 를 반환해 알림이 오지 않도록 한다.
        @Override
        public boolean checkNotification(String targetEmail) {
            return !(mTargetEmail.equals(targetEmail));
        }
    };

    // SQLite Read 핸들러
    // 디바이스에 저장되어 있는 SQLite 에서 새로운 채팅 내역을 읽어온다.
    // 읽어온 채팅은 ArrayList 에 추가된다.
    private final ReadHandler readHandler = new ReadHandler(this);
    private static class ReadHandler extends Handler{
        private final WeakReference<ChatActivity> mActivity;

        private ReadHandler(ChatActivity mActivity) {
            this.mActivity = new WeakReference<>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            ChatActivity activity = mActivity.get();
            activity.readHandlerMsg();
        }
    }

    private void readHandlerMsg(){
        //chatService.sendSocket(dbHelper.getCurrentGID(roomId),"read","subFlag",roomId,String.valueOf(dbHelper.getLastReadGID(roomId)),target,roomName,"none","none","none");
        //dbHelper.myGIDUpdate(roomId,mEmail,dbHelper.getCurrentGID(roomId));
        chatList = dbHelper.getChatList(mTargetEmail);
        chatAdapter.updateList(chatList);
        chatRv.scrollToPosition(chatAdapter.getItemCount()-1); // 최근 받은 메시지 위치로 스크롤 이동
    }


    // 채팅 업데이트 핸들러
    private final UpdateHandler updateHandler = new UpdateHandler(this);
    private static class UpdateHandler extends Handler {
        private final WeakReference<ChatActivity> mActivity;

        private UpdateHandler(ChatActivity activity){
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ChatActivity activity = mActivity.get();
            activity.updateHandlerMsg();
        }
    }

    // 채팅을 DB 에서 읽어와 업데이트하는 메소드
    private void updateHandlerMsg(){
        chatList = dbHelper.getChatList(mTargetEmail);
        chatAdapter.updateList(chatList);
        chatRv.scrollToPosition(chatAdapter.getItemCount()-1); // 최근 받은 메시지 위치로 스크롤 이동
    }

    @Override
    protected void onResume() {
        super.onResume();
        Message message = updateHandler.obtainMessage();
        updateHandler.sendMessage(message);
    }

    /**
     * Activity 가 onStart() 하는 경우 ChatService 와 바인드한다.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Intent bindIntent = new Intent(this,ChatService.class);
        bindService(bindIntent,chatConnection, Context.BIND_AUTO_CREATE);
    }


    /**
     * Activity 가 onStop() 하는 경우 ChatService 와 바인드를 푼다.
     */
    @Override
    protected void onStop() {
        unbindService(chatConnection);
        isBound = false;
        super.onStop();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myEmail = getSharedPreferences("login",MODE_PRIVATE).getString("email",null);
        Timber.tag("ChatActivity myEmail").d(myEmail);
        mTargetEmail = getIntent().getStringExtra("targetEmail");
        mTargetName = getIntent().getStringExtra("targetName");

        if(dbHelper == null){
            dbHelper = new DBHelper(getApplicationContext(),myEmail,null,1);
            dbHelper.chatDB();
        }

        setClickListeners(); // View 들 연결하고 Listener 연결.
        setChatRecyclerView(); // 채팅 RecyclerView 설정.
    }

    /*
     * 채팅 RecyclerView 를 설정한다.
     */
    private void setChatRecyclerView(){
        // LayoutManager 를 설정한다.
        // 채팅이 아래에서부터 쌓이도록 reverseLayout 를 true 로 한다.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(myEmail,chatList,getApplicationContext());

        chatRv.setLayoutManager(linearLayoutManager);
        chatRv.setAdapter(chatAdapter);
    }

    private void setClickListeners(){
        chatRv = findViewById(R.id.chatRv);
        chatSendEt = findViewById(R.id.chatSendEt);
        chatSendBtn = findViewById(R.id.chatSendBtn);
        ImageView chatCloseIv = findViewById(R.id.chatBackIv);  // Activity 닫기 버튼
        TextView chatTitleTv = findViewById(R.id.chatTitleTv);  // Toolbar 에 있는 Title TextView

        chatTitleTv.setText(mTargetName);

        chatSendBtn.setOnClickListener(this);
        chatCloseIv.setOnClickListener(this);

        /*
         * chatSendEt 에서 엔터키 ('\n') 을 눌렀을 때 sendMsg 을 호출하여 소켓에 채팅 메시지를 보낸다.
         * 입력한 값이 없으면 보내기 버튼을 보여주지 않는다.
         * 입력한 값이 있을 때만 보내기 버튼을 보여준다.
         */
        chatSendEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(s != null){
                    //if (s.charAt(s.length() - 1) == '\n') sendMsg(s.toString());
                    if(s.length() != 0){
                        chatSendBtn.setVisibility(View.VISIBLE);
                    }else{
                        chatSendBtn.setVisibility(View.GONE);
                    }
                }

            }
        });
    }

    /*
     * 메시지를 보내는 메소드
     *
     * 연결되어 있는 채팅 서비스를 이용하여 소켓에 메시지를 전달한다.
     */
    private void sendMsg(String msg){
        chatService.sendSocket(mTargetEmail,msg);
        chatSendEt.setText("");
        Message message = updateHandler.obtainMessage();
        updateHandler.sendMessage(message);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.chatSendBtn:
                sendMsg(chatSendEt.getText().toString());   // 채팅 보내기 버튼을 누를 때 메시지를 보내는 메소드를 호출한다.
                break;

            case R.id.chatBackIv:
                onBackPressed();        // chatBackIv 버튼을 눌렀을 때 Activity 를 종료한다.
                break;
        }
    }
}
