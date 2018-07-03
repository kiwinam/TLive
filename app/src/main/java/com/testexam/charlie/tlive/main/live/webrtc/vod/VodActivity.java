package com.testexam.charlie.tlive.main.live.webrtc.vod;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.common.BaseActivity;
import com.testexam.charlie.tlive.common.HttpTask;
import com.testexam.charlie.tlive.common.Params;
import com.testexam.charlie.tlive.main.live.webrtc.broadChat.Chat;
import com.testexam.charlie.tlive.main.live.webrtc.broadChat.ChatAdapter;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

/**
 * Vod 를 보여주는 Activity
 * Created by charlie on 2018. 5. 30
 */

@SuppressWarnings("deprecation")
public class VodActivity extends BaseActivity implements ExoPlayer.EventListener{
    private SimpleExoPlayer player;     // VOD 동영상을 재생할 EXOPlayer
    private DefaultTrackSelector trackSelector; // 동영상에서 재생할 부분을 선택하는 trackSelector
    private boolean shouldAutoPlay; // 자동 재생 여부

    private String vodUrl;  // 동영상의 URL

    private ProgressBar progressBar;    // 동영상 로드할 때 보여줄 프로그레스 바

    private String roomId;  // 방의 아이디

    private ArrayList<Chat> chatArrayList = new ArrayList<>();  // 현재 채팅 내역을 담고 있는 리스트
    private ArrayList<Chat> vodChatList = new ArrayList<>();    // 전체 VOD 채팅 내역을 담고 있는 리스트
    private ChatAdapter chatAdapter;    // 채팅 어댑터
    private RecyclerView vodChatRv;     // VOD 채팅 RecyclerView

    private Thread srtThread;   // 자막 스레드
    private boolean srtIsRun = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);

        shouldAutoPlay = true;

        vodUrl = getIntent().getStringExtra("vodUrl");  // 인텐트에서 VOD 의 경로를 가져온다.
        roomId = vodUrl.replace("/vod/","").replace(".m3u8","");    // 방의 아이디를 가져온다.
        Timber.tag("roomID").d(roomId);
    }

    /*
     * VOD 에 채팅을 보여주는 RecyclerView 를 설정한다.
     */
    private void setChatRecyclerView(){
        vodChatRv = findViewById(R.id.vodChatRv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());

        chatAdapter = new ChatAdapter(chatArrayList, getApplicationContext(), 1);

        vodChatRv.setLayoutManager(linearLayoutManager);
        vodChatRv.setAdapter(chatAdapter);
        getChatList();  // 채팅 내역을 가져온다
    }

    /*
     * 현재 시청하고 있는 VOD 에서 보냈던 채팅 내역들을 가져오는 메소드
     * VOD 의 roomId 를 파라미터로 서버에 채팅 내역을 요청한다.
     *
     * 요청의 응답은 보낸 이, 내용, 보낸 시간(ms) 로 구성되며 sender, message, time 으로 구분한다.
     * 가져온 채팅 내역은 ArrayList(vodChatList) 에 저장하고 현재 시청하고 있는 시간대에 오면 채팅 RecyclerView 에 추가한다.
     */
    private void getChatList(){
        ArrayList<Params> params = new ArrayList<>();   // 파라미터들을 가지고 있는 ArrayList 초기화
        params.add(new Params("roomId",roomId));    // roomId 를 파라미터에 추가한다.

        try {
            String result = new HttpTask("getLiveMsg.php",params).execute().get();  // getLiveMsg.php 로 채팅 내역을 요청한다.

            if(!result.isEmpty()){  // 채팅 내역이 있는 경우
                JSONArray chatArray = new JSONArray(result);    // 결과를 JSONArray 로 파싱한다.
                for(int i=0; i < chatArray.length(); i++){
                    JSONObject chatObject = chatArray.getJSONObject(i);
                    vodChatList.add(new Chat(   // 채팅 내역에 Chat 객체를 추가한다.
                            chatObject.getString("sender"),
                            chatObject.getString("message"),
                            chatObject.getInt("time")));
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        searchChatList();   // 채팅과 영상의 싱크를 맞춰 재생하는 메소드 호출
    }

    /*
     * 채팅과 동영상 싱크를 맞춰 채팅 RecyclerView 에 추가하는 메소드
     */
    private void searchChatList(){
        srtThread = new Thread(() -> {
            while(srtIsRun){    // srtIsRun 이 true 라면
                try {
                    Thread.sleep(100);  // 스레드를 0.1 초씩 멈추게한다.
                    long times = player.getCurrentPosition();   // 플레이어에 현재 재생 시간을 가져온다.
                    times = (long) Math.round(times/100) * 100; // 가져온 시간을 밀리 세컨드 단위로 바꾼다.

                    boolean isPut = false;  // 채팅 내역에 추가했는지 판단하는 변수
                    for(Chat chat : vodChatList){
                        if(chat.getTime() == times){    // 현재 재생 시간과 채팅 보낸 시간이 일치하면
                            chatArrayList.add(chat);    // 채팅 리스트에 추가한다.
                            isPut = true;   // isPut 변수를 true 로 변경한다.
                        }
                    }
                    if(isPut) { // 채팅 내역에 새로운 채팅이 들어갔다면
                        runOnUiThread(()->{
                            chatAdapter.setChatListData(chatArrayList); // 채팅 리스트 데이터가 변경되었음을 알려준다.
                            vodChatRv.scrollToPosition(chatAdapter.getItemCount()-1);   // 스크롤을 마지막 채팅 위치로 이동한다.
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        srtThread.start();
    }

    /*
     * VOD 시청을 위해 ExoPlayer 를 설정한다.
     *
     * 1. 기본 트랙 셀렉터 만들기
     * 2. 플레이어 생성
     * 3. 플레이어 설정
     * 4. 미디어 소스 가져오기
     */
    private void initVideoView(){
        // 1. 기본 트랙 셀렉터 만들기
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter); // 트렉 셀렉터 팩토리를 생성한다.
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);   // 기본 트렉 셀렉터를 생성한다.

        // 2. 플레이어 생성
        player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);    // 플레이어를 가져온다.
        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.vodPlayerView); // 레이아웃에서 플레이어 뷰를 연결한다.


        // 3. 플레이어 설정
        simpleExoPlayerView.setPlayer(player); // 뷰와 플레이어 바인딩
        simpleExoPlayerView.setUseController(true); // 컨트롤러 사용 설정
        simpleExoPlayerView.requestFocus();

        player.setPlayWhenReady(shouldAutoPlay);    // 미디어 소스가 준비되면 동영상을 재생한다.

        // 4. 미디어 소스 가져오기
        Handler mainHandler = new Handler();
        DataSource.Factory factory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"Tlive"),bandwidthMeter);
        HlsMediaSource hlsMediaSource = new HlsMediaSource(Uri.parse("http://13.125.64.135" + vodUrl),  // 서버에서 HLS 포맷으로 작성된 동영상을 가져온다.
                factory, mainHandler, new AdaptiveMediaSourceEventListener() {
            @Override
            public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) { }
            @Override
            public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) { }
            @Override
            public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {  }
            @Override
            public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) { }
            @Override
            public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) { }
            @Override
            public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) { }
        });
        player.prepare(hlsMediaSource); // 플레이어에 가져온 동영상을 넣는다.
        player.addListener(this);
        progressBar = findViewById(R.id.vodProgressBar);
        setChatRecyclerView();  // 채팅 리사이클러 뷰를 설정한다.
    }

    /*
     * 현재 Activity 를 종료하거나 화면이 보이지 않을 때 (onPause, onStop) 플레이어에 할당된 데이터를 풀어주는 메소드.
     */
    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay = player.getPlayWhenReady();
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    /*
     * onDestroy() 가 호출될 때 플레이어가 null 이 아니라면 플레이를 중지한다.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player != null){
            player.setPlayWhenReady(false); // 플레이 중지
        }
    }

    /*
     * Activity 가 onStart 되면 비디오 뷰를 설정한다.
     */
    @Override
    public void onStart() {
        super.onStart();
        initVideoView();    // 비디오 뷰 설정 시작
    }



    /*
     * 버퍼링이 있을 때 ProgressBar 를 보여주고, 버퍼링이 사라졌을 때 ProgressBar 를 지운다.
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState){
            case Player.STATE_BUFFERING:    // 버퍼링이 시작되면
                progressBar.setVisibility(View.VISIBLE);    // 프로그레스 바를 보여준다.
                break;
            case Player.STATE_IDLE:
                break;
            case Player.STATE_READY:    // 동영상이 준비되면
                progressBar.setVisibility(View.GONE);   // 프로그레스 바를 지운다.
                break;
            case Player.STATE_ENDED:
                break;
        }
    }

    /*
     * 플레이 중 에러가 발생했을 때 다이얼로그를 띄운다.
     */
    @Override
    public void onPlayerError(ExoPlaybackException error) {
        AlertDialog.Builder adb = new AlertDialog.Builder(VodActivity.this);
        adb.setTitle("VOD 재생 에러");
        adb.setMessage("VOD 재생 중 알 수 없는 에러가 발생하였습니다. 다시 시도해주세요.");
        adb.setPositiveButton("닫기", (dialog, which) -> {
            dialog.dismiss();   // 다이얼로그 지우기
            finish();   // 액티비티 종료
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    /*
     * 트렉 셀렉터에서 특정 지점을 클릭하거나 앞으로 10초가기 버튼 등을 눌러 새로운 구간을 찾을 때 채팅 싱크를 맞춘다.
     *
     * 1. 동영상 플레이어에서 현재 시간을 가져온다.
     * 2. 채팅 리스트를 초기화한다.
     * 3. 전체 채팅 리스트에서 현재 시간보다 이전에 있던 채팅을 채팅 리스트에 추가한다.
     * 4. 채팅 리사이클러 뷰에 설정한다.
     */
    @Override
    public void onSeekProcessed() {
        // 1. 동영상 플레이어에서 현재 시간을 가져온다.
        long currentPotion = player.getCurrentPosition();

        // 2. 채팅 리스트를 초기화한다.
        chatAdapter.clearAll();
        chatArrayList.clear();

        // 3. 전체 채팅 리스트에서 현재 시간보다 이전에 있던 채팅을 채팅 리스트에 추가한다.
        for(Chat chat : vodChatList){
            if(chat.getTime() <= currentPotion){
                chatArrayList.add(chat);
            }
            currentPotion+=100;
        }
        // 4. 채팅 리사이클러 뷰에 설정한다.
        chatAdapter.setChatListData(chatArrayList);
        vodChatRv.scrollToPosition(chatAdapter.getItemCount()-1);
    }

    /*
     * 비디오 뷰 설정을 시작한다.
     */
    @Override
    public void onResume() {
        super.onResume();
        if(player == null){
            initVideoView();
        }
    }

    /*
     * onPause 시 채팅과 동영상 싱크 맞추는 스레드를 중지한다.
     */
    @Override
    public void onPause() {
        srtIsRun = false;   // 동영상 싱크 중지
        releasePlayer();    // 플레이어 릴리스
        if(srtThread.isAlive()){    // 채팅 싱크 스레드가 살아있다면
            srtThread.interrupt();  // 스레드에 인터럽트 발생시킨다.
        }
        super.onPause();
    }

    /*
     * onStop 시 채팅과 동영상 싱크 맞추는 스레드를 중지한다.
     */
    @Override
    public void onStop() {
        srtIsRun = false;   // 동영상 싱크 중지
        if (Util.SDK_INT > 23) {
            releasePlayer();    // 플레이어 릴리스
        }
        if(srtThread.isAlive()){ // 채팅 싱크 스레드가 살아있다면
            srtThread.interrupt(); // 스레드에 인터럽트 발생시킨다.
        }
        super.onStop();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) { }
    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) { }
    @Override
    public void onLoadingChanged(boolean isLoading) { }
    @Override
    public void onRepeatModeChanged(int repeatMode) { }
    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) { }
    @Override
    public void onPositionDiscontinuity(int reason) { }
    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }
}
