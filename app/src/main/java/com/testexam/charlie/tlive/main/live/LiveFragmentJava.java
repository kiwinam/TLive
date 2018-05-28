package com.testexam.charlie.tlive.main.live;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity;
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity_;

import java.util.ArrayList;

/**
 * Created by charlie on 2018. 5. 28..
 */

public class LiveFragmentJava extends Fragment implements View.OnClickListener{
    private ArrayList<Broadcast> broadcastList = new ArrayList<>();
    private BroadcastAdapter adapter = null;

    private RecyclerView recyclerView;
    private ImageButton liveNewBtn;

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
        addBroadcast();
        setRecyclerView();
        setOnClickListeners();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.liveNewBtn:
                BroadCasterActivity_.intent(this).start();
                break;
        }
    }

    private void setRecyclerView(){
        adapter = new BroadcastAdapter(broadcastList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setOnClickListeners(){
        liveNewBtn.setOnClickListener(this);
    }

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
    }
}
