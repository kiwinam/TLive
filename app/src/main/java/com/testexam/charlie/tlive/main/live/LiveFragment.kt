package com.testexam.charlie.tlive.main.live


import android.Manifest
import android.content.Context
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
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity

import kotlinx.android.synthetic.main.fragment_live.*
import org.json.JSONArray

/**
 * 라이브 방송 프레그먼트
 *
 * 현재 진행되고 있는 라이브 방송과 이전에 방송했던 Vod 목록을 보여준다.
 * 라이브 방송을 클릭하면 라이브 방송을 시청할 수 있는 ViewerActivity 로 이동한다.
 * VOD 방송을 클릭하면 VOD 를 시청할 수 있는 VodActivity 로 이동한다.
 *
 * Created by charlie on 2018. 5. 24
 */
class LiveFragment : Fragment() , View.OnClickListener{
    private val broadcastList : ArrayList<Broadcast> = ArrayList()  // 라이브 방송과 VOD 의 리스트
    private lateinit var adapter : BroadcastAdapter // 방송 어댑터

    private var userEmail = ""  // 현재 로그인한 사용자의 이메일

    // LiveFragment 객체를 반환한다.
    companion object {
        fun newInstance(): LiveFragment = LiveFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_live,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userEmail = context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none") // SharedPreference 에서 이메일 정보를 가져온다.

        setOnClickListeners() // 클릭 리스너 설정
        setRecyclerView() // 리사이클러뷰 설정
        getBroadcastList()  // 방송 목록을 서버에 요청한다.
    }

    override fun onClick(v: View?) {
        when(v){
            // 새로운 라이브 방송 시작하기 버튼
            // 라이브 방송에 필요한 권한 (카메라, 마이크) 가 허용 되어 있다면 방송 화면으로
            // 아니라면 권한을 요구하는 화면으로 이동한다.
            liveNewBtn->{

                val micPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO)
                val cameraPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
                // 방송 시작에 필요한 모든 권한을 가지고 있다면
                if(micPermission == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(context, BroadCasterActivity::class.java)) // 방송 시작 액티비티로 이동
                // 방송 시작하는데 권한이 필요하다면
                }else{
                    startActivity(Intent(context,LivePermissionActivity::class.java))   // 권한 설정 액티비티로 이동
                }
            }
            // 새로운 글 남기기 레이아웃, 클릭시 글 쓰기 Activity 로 이동한다.
            //liveNewTextLo->startActivity(Intent(context,LivePermissionActivity::class.java))

            // 사람 모양 아이콘을 클릭하면 프로필 Fragment 를 불러온다.
            liveProfileIv->{

            }
        }
    }

    /*
     * 방송 리스트 RecyclerView 를 설정하는 메소드
     */
    private fun setRecyclerView(){
        adapter = BroadcastAdapter(broadcastList,context!!,fragmentManager!!)   // 방송 어댑터를 초기화한다.
        liveRv.layoutManager = LinearLayoutManager(context) // liveRv 에 레이아웃 매니저를 설정한다
        liveRv.adapter = adapter        // liveRv 에 어댑터를 설정한다.
        liveRv.isNestedScrollingEnabled = false     // liveRv 가 내부 스크롤에서도 스크롤이 부드럽게 이동하도록 설정한다.

        // 아래로 당겨 스와이프 할 때 리프레시 리스너
        liveSwipeRefreshLo.setOnRefreshListener({
            getBroadcastList()  // 리프레시를 할 때 방송 목록을 새로 요청한다.
        })
    }

    /* 뷰들에 클릭 리스너를 오버라이딩된 onClick 에 연결한다. */
    private fun setOnClickListeners(){
        liveNewBtn.setOnClickListener(this)
        liveNewTextLo.setOnClickListener(this)
        liveProfileIv.setOnClickListener(this)
    }

    /*
     * 서버에 방송 목록을 요청한다.
     *
     * 파라미터로 현재 로그인한 유저의 이메일을 담는다.
     * 서버에 방송 목록을 요청하고 결과 값이 있다면 broadcastList 에 추가하고 어댑터를 통해 RecyclerView 를 갱신한다.
     */
    private fun getBroadcastList(){
        Thread(Runnable {
            try{
                activity!!.runOnUiThread({
                    livePb.visibility = View.VISIBLE    // 프로그레스 바를 보여준다.
                })
                val paramList = ArrayList<Params>() // 파라미터들을 담고 있는 ArrayList
                paramList.add(Params("userEmail",userEmail))    // userEmail 을 파라미터에 추가한다.

                val result =  HttpTask("getBroadcastList.php", paramList).execute().get()   // 서버에 방송 목록을 요청한다. 결과 값은 result 에 저장한다.
                if(result != null){     // 방송 목록 결과가 있는 경우
                    val array = JSONArray(result)       // result 를 JSONArray 로 파싱한다.
                    broadcastList.clear()   // 방송 목록 초기화
                    for(i in 0..(array.length()-1)){
                        val responseObject = array.getJSONObject(i)
                        broadcastList.add(Broadcast // 방송 목록에 새로운 Broadcast 객체를 추가한다.
                        (responseObject.getString("hostEmail"),
                                (responseObject.getString("hostName")),
                                (responseObject.getString("hostProfileUrl")),
                                (responseObject.getInt("roomNo")),
                                (responseObject.getInt("roomSessionNo")),
                                (responseObject.getString("roomName")),
                                (responseObject.getString("roomTag")),
                                (responseObject.getInt("likeNum")),
                                (responseObject.getInt("viewerNum")),
                                (responseObject.getInt("isLive")),
                                (responseObject.getString("uploadTime")),
                                (responseObject.getString("previewSrc")),
                                (responseObject.getString("vodSrc")),
                                (responseObject.getBoolean("isLike")),
                                (responseObject.getBoolean("isSubscribe"))
                        ))
                    }
                    activity!!.runOnUiThread({
                        adapter.setBroadcastList(broadcastList)  // 어댑터에 broadcastList 의 데이터를 전달한다.

                        if(liveSwipeRefreshLo != null){
                            liveSwipeRefreshLo.isRefreshing = false // 스와이프 레이아웃에 리프레시를 종료한다.
                        }
                        livePb.visibility = View.GONE       // 프로그레스 바를 안보이게 한다.
                    })
                }
            } catch (e : Exception){
                e.printStackTrace()
            }
        }).start()
    }
}