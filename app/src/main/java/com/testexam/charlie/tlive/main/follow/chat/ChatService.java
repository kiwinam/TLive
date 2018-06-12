package com.testexam.charlie.tlive.main.follow.chat;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.testexam.charlie.tlive.common.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 네티 채팅 서버와 TCP 통신하는 소켓 채널 서비스
 * 채팅 메시지의 send, receive 를 담당한다.
 */
public class ChatService extends Service {
    // TCP 통신을 위한 서버의 IP 주소와 포트 번호
    private static final String IP = "13.125.64.135";
    private static final int PORT = 7777;
    private SocketChannel socketChannel;

    private JSONObject msgObject;

    private String email;
    private String name;

    private DBHelper dbHelper;

    private final IBinder mBinder = new ChatBinder();

    // 다른 액티비티에서 서비스를 바인드하기 위한 메소드.
    public class ChatBinder extends Binder{
        public ChatService getService() { return ChatService.this; }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public interface ReceiveCallback {
        void receiveMsg(String msg);
        boolean checkNotification(String targetEmail);
    }

    private ReceiveCallback receiveCallBack;

    public void registerCallback(ReceiveCallback callback) { receiveCallBack = callback; }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("ChatService","onCreate");
        SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
        email = sp.getString("email",null);
        name = sp.getString("name",null);

        // SQLite 연결
        if(dbHelper == null){
            dbHelper = new DBHelper(getApplicationContext(),email,null,1);
            dbHelper.chatDB();
        }

        // socket 연결
        if(email != null){
            try{
                setSocket();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    private void setSocket() throws IOException{
        new Thread(() -> {
            try {
                if(socketChannel == null){ // 소켓이 연결되어 있지 않은 경우에만 새로운 소켓 연결을 시도한다.
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(IP,PORT)); // AWS IP 와 Netty 가 청취하고 있는 포트로 연결을 시도한다.

                    // 소켓 연결이 성공하면 소켓에 이메일과 이름 정보를 설정한다.
                    if(socketChannel.isConnected()){
                        Log.d("socket","connected");
                        msgObject = new JSONObject();
                        msgObject.put("type","set");
                        msgObject.put("email",email);
                        msgObject.put("name",name);
                        socketChannel
                                .socket()
                                .getOutputStream()
                                .write(msgObject.toString().getBytes("EUC-KR")); // 서버로 전송
                        checkSocket.start(); // 소켓 채널을 청취하는 스레드를 시작한다.
                    }
                } else{
                    Log.e("ChatService::setSocket","socket already connected");
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Thread checkSocket = new Thread(){
        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            try{
                ByteBuffer byteBuffer;
                while(true){
                    byteBuffer = ByteBuffer.allocate(256);
                    int readByteCount = socketChannel.read(byteBuffer); // 소켓에서 데이터를 읽어서 byteBuffer 에 저장한다.
                    if(readByteCount == -1){
                        throw new IOException();
                    }

                    byteBuffer.flip();
                    Charset charset = Charset.forName("EUC_KR");
                    String data = charset.decode(byteBuffer).toString();
                    JSONObject msgObject= new JSONObject(data);
                    msgObject.put("isReceive",true); // 내가 받은 메시지라는 것을 표기한다.
                    dbHelper.receiveChat(msgObject.toString()); // 데이터 베이스에 받은 메시지 저장

                    if(receiveCallBack != null){
                        receiveCallBack.receiveMsg(msgObject.toString()); // 콜백 메서드 실행
                    }

                    // Notification 발생
//                    if(receiveCallBack != null){
//                        if(!receiveCallBack.checkNotification(msgObject.getString("senderEmail"))){
//                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                            Intent notiIntent = new Intent(getApplicationContext(), ChatActivity.class);
//                            notiIntent.putExtra("targetEmail",msgObject.getString("senderEmail"));
//                            notiIntent.putExtra("targetName",msgObject.getString("senderName"));
//                            notiIntent.putExtra("flag","noti");
//                            int notiNum = dbHelper.getRoomNum(msgObject.getString("senderEmail"));
//                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),notiNum,notiIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//                            Notification.Builder builder = new Notification.Builder(getApplicationContext());
//                            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
//                            builder.setSmallIcon(R.mipmap.ic_launcher_round);
//                            builder.setContentTitle(msgObject.getString("senderName"));
//                            builder.setWhen(System.currentTimeMillis());
//                            builder.setContentIntent(pendingIntent);
//                            builder.setPriority(Notification.PRIORITY_MAX);
//                            builder.setAutoCancel(true);
//                            builder.setFullScreenIntent(pendingIntent,true);
//                            notificationManager.notify(notiNum,builder.build());
//                        }
//                        Log.d("receive", "msg : "+data);
//
//                    }
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

    public void sendSocket(String targetEmail, String msg){
        msgObject = new JSONObject();
        new Thread(() -> {
            try{
                msgObject.put("type","msg");
                msgObject.put("senderEmail",email);
                msgObject.put("senderName",name);
                msgObject.put("targetEmail",targetEmail);
                msgObject.put("msg",msg);

                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(msgObject.toString().getBytes("EUC-KR")); // 서버로 전송
                msgObject.put("isReceive",false);
                // 내가 보낸 메시지는 바로 내부 SQLite 에 저장한다.
                dbHelper.receiveChat(msgObject.toString());
            }catch (JSONException | IOException e){
                e.printStackTrace();
            }
        }).start();
    }
}
