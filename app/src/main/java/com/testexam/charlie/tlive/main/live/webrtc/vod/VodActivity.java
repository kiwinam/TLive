package com.testexam.charlie.tlive.main.live.webrtc.vod;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

/**
 * Vod 를 보여주는 Activity
 * Created by charlie on 2018. 5. 30
 */

public class VodActivity extends BaseActivity implements ExoPlayer.EventListener{

    private SimpleExoPlayer player;

    private DefaultTrackSelector trackSelector;
    private boolean shouldAutoPlay;

    private String vodUrl;
    private ProgressBar progressBar;

    private String roomId;

    private ArrayList<Chat> chatArrayList = new ArrayList<>();
    private ArrayList<Chat> vodChatList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private RecyclerView vodChatRv;

    private Thread srtThread;
    private boolean srtIsRun = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);

        shouldAutoPlay = true;

        vodUrl = getIntent().getStringExtra("vodUrl");
        // /vod/20180604054907_email@naver.com.m3u8
        roomId = vodUrl.replace("/vod/","").replace(".m3u8","");
        Log.d("roomID",roomId);
        //setChatRecyclerView();
        //player.getPlaybackLooper().getThread().
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
        getChatList();
    }

    /*
     * 현재 시청하고 있는 VOD 에서 보냈던 채팅 내역들을 가져오는 메소드
     * VOD 의 roomId 를 파라미터로 서버에 채팅 내역을 요청한다.
     *
     * 요청의 응답은 보낸 이, 내용, 보낸 시간(ms) 로 구성되며 sender, message, time 으로 구분한다.
     * 가져온 채팅 내역은 ArrayList(vodChatList) 에 저장하고 현재 시청하고 있는 시간대에 오면 채팅 RecyclerView 에 추가한다.
     */
    private void getChatList(){
        ArrayList<Params> params = new ArrayList<>();
        params.add(new Params("roomId",roomId));

        /*new Thread(() -> {

        }).start();*/
        try {
            String result = new HttpTask("getLiveMsg.php",params).execute().get();
            Log.d("result : ",result);
            if(!result.isEmpty()){
                JSONArray chatArray = new JSONArray(result);
                for(int i=0; i < chatArray.length(); i++){
                    JSONObject chatObject = chatArray.getJSONObject(i);
                    vodChatList.add(new Chat(
                            chatObject.getString("sender"),
                            chatObject.getString("message"),
                            chatObject.getInt("time")));
                    Log.d( chatObject.getInt("time")+" ) sender : ",chatObject.getString("sender") +" >> " + chatObject.getString("message"));
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        searchChatList();
    }

    private void searchChatList(){
        srtThread = new Thread(() -> {
            while(srtIsRun){
                try {
                    Thread.sleep(100);
                    long times = player.getCurrentPosition();
                    times = (long) Math.round(times/100) * 100;

                    boolean isPut = false;
                    for(Chat chat : vodChatList){
                        if(chat.getTime() == times){
                            chatArrayList.add(chat);
                            isPut = true;
                        }
                    }
                    if(isPut) {
                        runOnUiThread(()->{
                            chatAdapter.setChatListData(chatArrayList);
                            vodChatRv.scrollToPosition(chatAdapter.getItemCount()-1);
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
     */
    private void initVideoView(){
        // 1. 기본 트랙 셀렉터 만들기
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. 플레이어 생성
        player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);
        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.vodPlayerView);


        // 3. 플레이어 설정
        simpleExoPlayerView.setPlayer(player); // 뷰와 플레이어 바인딩
        simpleExoPlayerView.setUseController(true); // 컨트롤러 사용 설정
        simpleExoPlayerView.requestFocus();

        player.setPlayWhenReady(shouldAutoPlay);

        // 4. 미디어 소스 가져오기
        Handler mainHandler = new Handler();
        DataSource.Factory factory = new DefaultDataSourceFactory(this, Util.getUserAgent(this,"Tlive"),bandwidthMeter);
        HlsMediaSource hlsMediaSource = new HlsMediaSource(Uri.parse("http://13.125.64.135" + vodUrl),
                factory, mainHandler, new AdaptiveMediaSourceEventListener() {
            @Override
            public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {
            }

            @Override
            public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
            }

            @Override
            public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
                Log.d("hlsMediaSource","onLoadCanceled");
            }

            @Override
            public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
                Log.d("hlsMediaSource","onLoadError");
            }

            @Override
            public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {
                Log.d("hlsMediaSource","onUpstreamDiscarded");
            }

            @Override
            public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {
                Log.d("hlsMediaSource","onDownstreamFormatChanged");
            }
        });
        player.prepare(hlsMediaSource);
        player.addListener(this);
        progressBar = findViewById(R.id.vodProgressBar);
        setChatRecyclerView();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player != null){
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initVideoView();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Log.d("click","onTimelineChanged");
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.d("click","onTracksChanged");

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.d("click","onLoadingChanged");
    }

    /*
     * 버퍼링이 있을 때 ProgressBar 를 보여주고, 버퍼링이 사라졌을 때 ProgressBar 를 지운다.
     */
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState){
            case Player.STATE_BUFFERING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case Player.STATE_IDLE:
                break;
            case Player.STATE_READY:
                progressBar.setVisibility(View.GONE);
                break;
            case Player.STATE_ENDED:
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        Log.d("click","onRepeatModeChanged");
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) { }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        AlertDialog.Builder adb = new AlertDialog.Builder(VodActivity.this);
        adb.setTitle("Could not able to stream video");
        adb.setMessage("It seems that something is going wrong.\nPlease try again.");
        adb.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Log.d("click","onPositionDiscontinuity");
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }

    @Override
    public void onSeekProcessed() {
        Log.d("click","onSeekProcessed");
        long currentPotion = player.getCurrentPosition();
        chatAdapter.clearAll();
        chatArrayList.clear();
        for(Chat chat : vodChatList){
            if(chat.getTime() <= currentPotion){
                chatArrayList.add(chat);
            }
            currentPotion+=100;
        }
        chatAdapter.setChatListData(chatArrayList);
        vodChatRv.scrollToPosition(chatAdapter.getItemCount()-1);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(player == null){
            initVideoView();
        }
    }

    @Override
    public void onPause() {
        srtIsRun = false;
        releasePlayer();
        if(srtThread.isAlive()){
            srtThread.interrupt();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        srtIsRun = false;
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
        if(srtThread.isAlive()){
            srtThread.interrupt();
        }
        super.onStop();
    }

}
