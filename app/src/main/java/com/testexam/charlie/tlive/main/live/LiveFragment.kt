package com.testexam.charlie.tlive.main.live


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity


import kotlinx.android.synthetic.main.fragment_live.*

/**
 * 라이브 방송 프레그먼트
 *
 * Created by charlie on 2018. 5. 24
 */
class LiveFragment : Fragment() , View.OnClickListener{
    private val broadcastList : ArrayList<Broadcast> = ArrayList()
    private var adapter : BroadcastAdapter? = null

    // LiveFragment 객체를 반환한다.
    companion object {
        fun newInstance(): LiveFragment = LiveFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_live,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        addBroadcast() // 테스트 데이터를 넣는다.
        setOnClickListeners() // 클릭 리스너 설정
        setRecyclerView() // 리사이클러뷰 설정
    }

    override fun onClick(v: View?) {
        when(v){
            // 새로운 라이브 방송 시작하기 버튼
            // 라이브 방송에 필요한 권한 (카메라, 마이크) 가 허용 되어 있다면 방송 화면으로
            // 아니라면 권한을 요구하는 화면으로 이동한다.
            liveNewBtn->{

                val micPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                val cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

                if(micPermission == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED){
                    // 방송 시작에 필요한 모든 권한을 가지고 있다면
                    startActivity(Intent(context, BroadCasterActivity::class.java))
                    //BroadCasterActivity.intent(this).start();
                }else{
                    // 방송 시작하는데 권한이 필요하다면
                    startActivity(Intent(context,LivePermissionActivity::class.java))
                }
            }
            // 새로운 글 남기기 레이아웃, 클릭시 글 쓰기 Activity 로 이동한다.
            //liveNewTextLo->startActivity(Intent(context,LivePermissionActivity::class.java))

            // 사람 모양 아이콘을 클릭하면 프로필 Fragment 를 불러온다.
            liveProfileIv->{

            }
        }
    }

    private fun setRecyclerView(){
        adapter = BroadcastAdapter(broadcastList,context)
        liveRv.layoutManager = LinearLayoutManager(context)
        liveRv.adapter = adapter

        liveSwipeRefreshLo.setOnRefreshListener({
            addBroadcast()
        })
        liveRv.viewTreeObserver.addOnScrollChangedListener ({

        })
    }

    private fun setOnClickListeners(){
        liveNewBtn.setOnClickListener(this)
        liveNewTextLo.setOnClickListener(this)
        liveProfileIv.setOnClickListener(this)
    }

    fun addBroadcast(){
        broadcastList.add(
                Broadcast(0,0,"박천명",
                        "none","치킨 먹방",
                        "#치킨 #먹방 #가즈아","none",1))
        broadcastList.add(
                Broadcast(0,0,"박천명",
                        "none","치킨 먹방",
                        "#치킨 #먹방 #가즈아","none",0))
        broadcastList.add(
                Broadcast(0,0,"박천명",
                        "none","치킨 먹방",
                        "#치킨 #먹방 #가즈아","none",1))
        broadcastList.add(
                Broadcast(0,0,"박천명",
                        "none","치킨 먹방",
                        "#치킨 #먹방 #가즈아","none",0))
        adapter?.setData(broadcastList)
        adapter?.notifyDataSetChanged()
        liveSwipeRefreshLo.isRefreshing = false;
    }
}