package com.testexam.charlie.tlive.main.follow.chat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.common.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Objects;

import timber.log.Timber;

/**
 * 네티 채팅 서버와 TCP 통신하는 소켓 채널 서비스
 * 채팅 메시지의 send, receive 를 담당한다.
 */
@SuppressLint("LogNotTimber")
public class ChatService extends Service {
    // TCP 통신을 위한 서버의 IP 주소와 포트 번호
    private static final String IP = "13.125.64.135";   // 서버 ip 주소
    private static final int PORT = 7777;       // 네티 포트 번호
    private static final int SERVICE_ID = 8888; // 포그라운드 서비스 아이디
    private SocketChannel socketChannel;        // 소켓 채널 객체

    private JSONObject msgObject;   // 채팅을 주고 받을 때 사용하는 JSONObject

    private String email;   // 현재 로그인한 사용자의 이메일
    private String name;    // 현재 로그인한 사용자의 이름

    private DBHelper dbHelper;  // 디바이스 내장 SQLite 를 사용할 수 있게 도와주는 DBHelper 객체
    private ReceiveCallback receiveCallBack;    // ReceiveCallback 객체.
    private final IBinder mBinder = new ChatBinder();

    // 다른 액티비티에서 서비스를 바인드하기 위한 메소드.
    public class ChatBinder extends Binder{
        public ChatService getService() { return ChatService.this; }    // getService 메소드를 호출하면 ChatService 자신을 리턴해준다.
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /*
     * 채팅 서비스에서 꼭 등록해야하는 콜백 메소드
     *
     * 채팅 서비스를 바인드해서 쓰는 Activity 에서 콜백 메소드를 통해 각 메소드에 맞는 행동을 할 수 있다.
     */
    public interface ReceiveCallback {
        void receiveMsg(String msg);        // 메시지를 받았을 때 호출되는 메소드
        boolean checkNotification(String targetEmail);  // 새로운 메시지를 받았을 때 Notification 을 생성할지 결정하는 메소드
    }

    /*
     * 다른 Activity 에서 채팅 서비스에 바인드 할 때 ReceiveCallback 객체를 초기화하는 메소드
     */
    public void registerCallback(ReceiveCallback callback) { receiveCallBack = callback; }  // ReceiveCallback 를 매개변수로 전달된 callback 으로 초기화한다.

    /*
     * 채팅 서비스가 생성되었을 때 호출되는 메소드
     *
     * 채팅 서비스가 생성되면 유저의 정보를 가져온다.
     * SQLite 에 연결한다.
     * 서버와 소켓을 연결한다.
     */
    @Override
    public void onCreate() {
        if(!isServiceRunning()){    // 채팅 서비스가 실행중이지 않은 경우에만 새로운 채팅 서비스를 시작한다.
            super.onCreate();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /*Notification notification = new Notification();
            startForeground(SERVICE_ID,notification);*/
            }

            //Timber.tag("ChatService").e("onCreate");
            Log.e("ChatService","onCreate");
            SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);  // SharedPreference 에서 사용자의 정보를 가져온다.
            email = sp.getString("email",null);     // 이메일을 가져온다
            name = sp.getString("name",null);       // 이름을 가져온다.

            // SQLite 연결
            if(dbHelper == null){   // dbHelper 가 초기화 되어 있지 않다면
                dbHelper = new DBHelper(getApplicationContext(),email,null,1);  // dbHelper 를 현재 로그인한 사용자의 이메일을 매개변수로 초기화한다.
                dbHelper.chatDB();      // dbHelper 를 쓰기 상태로 변경한다.
            }

            // socket 연결
            if(email != null){
                setSocket();    // 네티 소켓에 연결한다.
            }
        }
    }

    /*
     * 네티 소켓에 연결하는 메소드
     *
     * 네티에서 새로운 소켓을 생성하고 소켓에 연결된 사용자가 누구인지 알려준다.
     * 소켓 설정이 성공하면 네티 소켓에서 들어오는 메시지를 청취하는 스레드를 시작한다.
     *
     * 1. 네티 소켓 채널이 없는 경우 새로운 소켓(IP,PORT)을 만든다.
     * 2. 소켓 연결이 성공하면 소켓에 이메일과 이름 정보를 설정한다.
     * 3. 소켓에서 새로운 메시지를 청취하는 스레드를 시작한다.
     */

    private void setSocket() {
        new Thread(() -> {
            try {
                // 1. 네티 소켓 채널이 없는 경우 새로운 소켓(IP,PORT)을 만든다.
                if(socketChannel == null){
                    socketChannel = SocketChannel.open();   // 새로운 SocketChannel 오픈
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(IP,PORT)); // AWS IP 와 Netty 가 청취하고 있는 포트로 연결을 시도한다.

                    // 2. 소켓 연결이 성공하면 소켓에 이메일과 이름 정보를 설정한다.
                    if(socketChannel.isConnected()){    // 소켓 연결이 성공한 경우
                        //Timber.tag("socket").d("connected");
                        Log.d("socket","connected");
                        msgObject = new JSONObject();   // 소켓 정보를 담을 JSONObject
                        msgObject.put("type","set");    // 소켓에 전달되는 메시지의 타입이 설정 메시지라는 것을 알려준다.
                        msgObject.put("email",email);       // 소켓에 연결된 이메일
                        msgObject.put("name",name);         // 소켓에 연결된 이름
                        socketChannel
                                .socket()                           // 소켓에 있는
                                .getOutputStream()                  // OutputStream 에
                                .write(msgObject.toString().        // msgObject 의 값을 넣고
                                getBytes("EUC-KR"));  // 인코딩 타입을 EUC-KR 로 설정한 다음 서버에 소켓 채널을 통해 전송한다.

                        // 3. 소켓에서 새로운 메시지를 청취하는 스레드를 시작한다.
                        checkSocket.start();
                    }
                } else{
                    //Timber.tag("ChatService::setSocket").e("socket already connected");
                    Log.e("ChatService::setSocket","socket already connected");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start(); // 소켓 채널 설정 스레드 시작.
    }

    /*
     * 네티 소켓 채널에서 새로운 메시지를 청취하는 스레드
     *
     * 1. 네티 소켓 채널에서 새로운 메시지가 올 때 까지 기다린다.
     * 2. 새로운 메시지가 도착하면 byteBuffer 에 담은뒤 String 형태로 메시지를 가져온다.
     * 3. SQLite 에 메시지를 저장한다.
     * 4. 콜백에 있는 receiveMsg 메서드를 통해 새로운 메시지가 왔을 때 Binding 된 상황에 맞는 액션을 취한다.
     */
    private Thread checkSocket = new Thread(){
        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            try{
                ByteBuffer byteBuffer;
                //  1. 네티 소켓 채널에서 새로운 메시지가 올 때 까지 기다린다.
                while(true){
                    byteBuffer = ByteBuffer.allocate(256);  // ByteBuffer 에 힙 메모리 256 Byte 를 할당한다.
                    //  2. 새로운 메시지가 도착하면 byteBuffer 에 담은뒤 String 형태로 메시지를 가져온다.
                    int readByteCount = socketChannel.read(byteBuffer); // 소켓에서 데이터를 읽어서 byteBuffer 에 저장한다.
                    if(readByteCount == -1){    // 소켓채널에서 들어온 바이트의 길이가 -1 이라면 에러를 발생시킨다.
                        throw new IOException();
                    }

                    byteBuffer.flip();  // 바이트 버퍼의 포지션을 0으로 설정하고, limit 를 현재 내용의 마지막 위치로 압축한다.
                    Charset charset = Charset.forName("EUC_KR");    // 캐릭터셋을 EUC-KR 로 설정한다.
                    String data = charset.decode(byteBuffer).toString();    // byteBuffer 에서 EUC-KR 로 데이터를 가져와 data 변수에 저장한다.
                    JSONObject msgObject= new JSONObject(data); // 가져온 data 를 JSONObject 로 파싱한다.
                    msgObject.put("isReceive",true); // 내가 받은 메시지라는 것을 표기한다.
                    dbHelper.receiveChat(msgObject.toString()); // 데이터 베이스에 받은 메시지 저장

                    if(receiveCallBack != null){        // receiveCallBack 이 등록되어 있다면
                        receiveCallBack.receiveMsg(msgObject.toString()); // 콜백 메서드 실행
                    }

                    /*
                     * 새로운 채팅이 왔을 때 ReceiveCallback 에서 checkNotification 메소드를 통해 노티 발생 여부를 체크할 때 에러(NullPointException) 발생
                     * 개발 우선순위상 급한 기능이 아니라 일단 주석처리해뒀음
                     *
                     * 체크 리스트
                     * 1. ChatActivity 가 onResume() 상태일 때는 에러가 나오지 않음 - 2018.06.09 박천명 (Charlie Park)
                     * 2. Oreo 버전 업그레이드 이후 startService 가 정상적으로 동작하지 않는 이슈 확인 - 2018.06.20 박천명 (Charlie Park)
                     *
                     * 2018.06.08 박천명 (Charlie Park)
                     *
                    // Notification 발생
                    if(receiveCallBack != null){
                        if(!receiveCallBack.checkNotification(msgObject.getString("senderEmail"))){
                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            Intent notiIntent = new Intent(getApplicationContext(), ChatActivity.class);
                            notiIntent.putExtra("targetEmail",msgObject.getString("senderEmail"));
                            notiIntent.putExtra("targetName",msgObject.getString("senderName"));
                            notiIntent.putExtra("flag","noti");
                            int notiNum = dbHelper.getRoomNum(msgObject.getString("senderEmail"));
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),notiNum,notiIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                            Notification.Builder builder = new Notification.Builder(getApplicationContext());
                            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
                            builder.setSmallIcon(R.mipmap.ic_launcher_round);
                            builder.setContentTitle(msgObject.getString("senderName"));
                            builder.setWhen(System.currentTimeMillis());
                            builder.setContentIntent(pendingIntent);
                            builder.setPriority(Notification.PRIORITY_MAX);
                            builder.setAutoCancel(true);
                            builder.setFullScreenIntent(pendingIntent,true);
                            notificationManager.notify(notiNum,builder.build());
                        }
                        Log.d("receive", "msg : "+data);

                    }
                    */
                }
            }catch (IOException|JSONException e){
                e.printStackTrace();
                try{
                    socketChannel.close();

                }catch (IOException ee){
                    ee.printStackTrace();
                }
            }
        }
    };

    /*
     * 네티 소켓 채널에 메시지를 보내는 메소드
     *
     * 1. JSONObject 에 채팅 데이터를 추가한다.
     * 2. 네티 소켓 채널을 통해 서버로 전송한다.
     * 3. SQLite 에 새로운 메시지를 추가한다.
     */
    public void sendSocket(String targetEmail, String msg){
        // 1. JSONObject 에 채팅 데이터를 추가한다.
        msgObject = new JSONObject();   // 채팅 메시지를 담을 JSONObject 을 초기화한다.
        new Thread(() -> {
            try{
                msgObject.put("type","msg");     // 메시지의 타입을 msg 로 설정하여 서버에서 메세지로 처리할 수 있게 한다.
                msgObject.put("senderEmail",email);     // 보낸 사람의 이메일을 현재 로그인한 유저의 이메일로 설정한다.
                msgObject.put("senderName",name);       // 보낸 사람의 이름을 현재 로그인한 유저의 이름으로 설정한다.
                msgObject.put("targetEmail",targetEmail);   // 받는 사람의 이메일을 설정한다.
                msgObject.put("msg",msg);       // 채팅 메시지를 설정한다.

                // 2. 네티 소켓 채널을 통해 서버로 전송한다.
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(msgObject.toString().getBytes("EUC-KR")); // 서버로 전송

                msgObject.put("isReceive",false);   // 내가 보낸 메시지라 표시한다.

                //  3. SQLite 에 새로운 메시지를 추가한다.
                dbHelper.receiveChat(msgObject.toString()); // 내가 보낸 메시지는 바로 내부 SQLite 에 저장한다.
            }catch (JSONException | IOException e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        try {
            if(socketChannel != null){
                if(socketChannel.isConnected()){
                    socketChannel.finishConnect();
                    socketChannel.close();
                    Log.e("socket","onDestroy");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    // 서비스의 실행 여부를 확인하는 메소드
    private boolean isServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : Objects.requireNonNull(activityManager).getRunningServices(Integer.MAX_VALUE)) {
            System.out.println(runningServiceInfo.service.getClassName());
            if (ChatService.class.getName().equals(runningServiceInfo.service.getClassName())) {
                Log.e("isServiceRunning","already running ChatService");
                return true;
            }
        }
        Log.e("isServiceRunning","new ChatService");
        return false;   // 실행중이지 않다.
    }
}
