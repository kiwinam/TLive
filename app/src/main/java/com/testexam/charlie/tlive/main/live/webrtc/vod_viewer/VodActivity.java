package com.testexam.charlie.tlive.main.live.webrtc.vod_viewer;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
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

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);

        shouldAutoPlay = true;

        vodUrl = getIntent().getStringExtra("vodUrl");

    }

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
                Log.d("hlsMediaSource","onLoadStarted");
            }

            @Override
            public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
                Log.d("hlsMediaSource","onLoadCompleted");
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
        /*MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("http://13.125.64.135"+vodUrl),
                mediaDataSourceFactory, extractorsFactory, null, null);*/

        player.prepare(hlsMediaSource);

        progressBar = findViewById(R.id.vodProgressBar);
    }


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
    public void onTimelineChanged(Timeline timeline, Object manifest) { }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) { }

    @Override
    public void onLoadingChanged(boolean isLoading) { }

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
    public void onRepeatModeChanged(int repeatMode) { }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) { }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        AlertDialog.Builder adb = new AlertDialog.Builder(VodActivity.this);
        adb.setTitle("Could not able to stream video");
        adb.setMessage("It seems that something is going wrong.\nPlease try again.");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish(); // take out user from this activity. you can skip this
            }
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {  }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) { }

    @Override
    public void onSeekProcessed() { }
    @Override
    public void onResume() {
        super.onResume();
        if(player == null){
            initVideoView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

}
