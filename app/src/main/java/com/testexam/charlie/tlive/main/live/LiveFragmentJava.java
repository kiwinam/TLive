package com.testexam.charlie.tlive.main.live;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity;

import java.util.ArrayList;

/**
 * Created by charlie on 2018. 5. 28..
 */

public class LiveFragmentJava extends Fragment implements View.OnClickListener{
    private ArrayList<Broadcast> broadcastList = new ArrayList<>();
    private BroadcastAdapter adapter = null;

    private RecyclerView recyclerView;
    private ImageButton liveNewBtn;
    private ImageView liveProfileIv;
    public static LiveFragmentJava newInstance(){
        return new LiveFragmentJava();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live, container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.liveRv);
        liveNewBtn = view.findViewById(R.id.liveNewBtn);
        liveProfileIv = view.findViewById(R.id.liveProfileIv);
        //addBroadcast();
        setRecyclerView();
        setOnClickListeners();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.liveNewBtn:
                //방송 시작 전에 마이크와 카메라 권한을 확인함.
                int micPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO);
                int cameraPermission = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);

                if(micPermission == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED){
                    // 방송 시작에 필요한 모든 권한을 가지고 있다면

                    //BroadCasterActivity_.intent(this).start();
                    //BroadCasterActivity.intent(this).start();
                }else{
                    // 방송 시작하는데 권한이 필요하다면
                    startActivity(new Intent(getContext(),LivePermissionActivity.class));
                }
                break;
            case R.id.liveProfileIv:
                //ViewerActivity_.intent(this).start();
        }
    }

    private void setRecyclerView(){
        adapter = new BroadcastAdapter(broadcastList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setOnClickListeners(){
        liveNewBtn.setOnClickListener(this);
        liveProfileIv.setOnClickListener(this);
    }
/*
    private void addBroadcast(){
        broadcastList.add(new Broadcast(0,0,"박천명",
                "none","치킨 먹방",
                "#치킨 #먹방 #가즈아","none",1));
        broadcastList.add(new Broadcast(0,0,"박천명",
                "none","치킨 먹방",
                "#치킨 #먹방 #가즈아","none",1));
        broadcastList.add(new Broadcast(0,0,"박천명",
                "none","치킨 먹방",
                "#치킨 #먹방 #가즈아","none",1));
        broadcastList.add(new Broadcast(0,0,"박천명",
                "none","치킨 먹방",
                "#치킨 #먹방 #가즈아","none",1));
    }*/
}
