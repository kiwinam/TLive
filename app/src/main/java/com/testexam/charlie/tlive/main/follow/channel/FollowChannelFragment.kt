package com.testexam.charlie.tlive.main.follow.channel

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import kotlinx.android.synthetic.main.fragment_follow_channel.*
import org.json.JSONArray

/**
 * 팔로우한 채널 리스트를 보여주는 Fragment
 *
 * Created by charlie on 2018. 5. 24..
 */
class FollowChannelFragment : Fragment(){
    private lateinit var onlineList : ArrayList<Channel>    // 온라인 채널 리스트
    private lateinit var offlineList : ArrayList<Channel>   // 오프라인 채널 리스트
    private lateinit var onlineAdapter: ChannelAdapter      // 온라인 채널 어댑터
    private lateinit var offlineAdapter: ChannelAdapter     // 오프라인 채널 어댑터
    private var userEmail = ""      // 현재 사용자의 이메일

    companion object {
        fun newInstance(): FollowChannelFragment = FollowChannelFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow_channel,container,false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userEmail = context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none") // 현재 사용자의 이메일을 SharedPreference 에서 가져온다.

        setRecyclerViews() // RecyclerView 설정

        // 스와이프 레이아웃에 리스너를 설정한다.
        // 스와이프 할 경우 팔로우 리스트를 요청한다.
        channelSwipeLo.setOnRefreshListener({
            getFollowChannels() // 서버에 팔로우 리스트 요청
        })
    }

    /*
     * 온라인과 오프라인의 RecyclerView 를 설정하는 메소드
     */
    private fun setRecyclerViews(){
        // 온라인 RecyclerView 설정
        onlineList = ArrayList() // 온라인 리스트 초기화
        onlineAdapter = ChannelAdapter(onlineList,context!!) // 온라인 어댑터 생성
        channelOnlineRv.adapter = onlineAdapter // 온라인 RecyclerView 에 어댑터 설정
        channelOnlineRv.layoutManager = LinearLayoutManager(context!!) // 온라인 RecyclerView 에 레이아웃 매니저 설정

        // 오프라인 RecyclerView 설정
        offlineList = ArrayList() // 오프라인 리스트 초기화
        offlineAdapter = ChannelAdapter(offlineList,context!!) // 오프라인 어댑터 생성
        channelOfflineRv.adapter = offlineAdapter   // 오프라인  RecyclerView 에 어댑터 설정
        channelOfflineRv.layoutManager = LinearLayoutManager(context!!) // 오프라인 RecyclerView 에 레이아웃 매니저 설정

        // 서버에 팔로우하고 있는 채널 데이터 요청
        getFollowChannels()
    }

    /*
     * 서버에 팔로우하고 있는 채널 데이터를 요청한다.
     *
     * 파라미터는 현재 사용자의 이메일이 담긴다.
     * 리턴값을 json 형태이고 리턴 데이터는 채널의 이름, 이메일, 프로필 사진 경로, 팔로우 수, 현재 방송 유무이다.
     * 리턴된 값을 현재 방송 유무에 맞춰 onlineList, offlineList 에 담고 각 RecyclerView 를 갱신한다.
     */
    private fun getFollowChannels(){
        Thread{
            try{
                activity!!.runOnUiThread({ channelPb.visibility = View.VISIBLE }) // 프로그레스 바를 보이게한다.
                val paramList = ArrayList<Params>() // 파라미터를 담을 ArrayList 초기화
                paramList.add(Params("email",userEmail)) // 현재 사용중인 사용자의 이메일을 파라미터에 담는다.

                // 서버에 팔로우 리스트를 요청하고 결과 값을 result 변수에 저장한다.
                val result = HttpTask("getFollowList.php",paramList).execute().get()
                if(result != "null"){ // 팔로우 리스트가 있는 경우
                    onlineList.clear() // 온라인 리스트 초기화
                    offlineList.clear() // 오프라인 리스트 초기화
                    val followJSONArray = JSONArray(result) // JSONArray 형식으로 result 를 파싱한다.
                    for(i in 0 until followJSONArray.length()){ // JSONArray 의 길이만큼 리스트 추가 작업을 진행한다.
                        val followObject = followJSONArray.getJSONObject(i) // 현재 반복문 포지션의 JSONObject 를 가져온다.
                        if(followObject.getInt("nowLive") == 1){  // 현재 온라인 상태인 (라이브 방송 중인) 채널일 때
                            onlineList.add(Channel(     // 온라인 채널 리스트에 새로운 채널 객체를 추가한다.
                                    followObject.getString("name"),         // 채널 호스트 이름
                                    followObject.getString("email"),        // 채널 호스트 이메일
                                    followObject.getString("profileSrc"),   // 채널 호스트 프로필 사진 경로
                                    followObject.getInt("followerNum"),     // 채널 호스트 팔로우 숫자
                                    followObject.getInt("nowLive")           // 현재 방송 유무
                            ))
                        }else{  // 현재 오프라인 상태인 채널일 때 (라이브 방송을 진행하지 않을 때)
                            offlineList.add(Channel(    // 오프라인 채널 리스트에 새로운 채널 객체를 추가한다.
                                    followObject.getString("name"),         // 채널 호스트 이름
                                    followObject.getString("email"),        // 채널 호스트 이메일
                                    followObject.getString("profileSrc"),   // 채널 호스트 프로필 사진 경로
                                    followObject.getInt("followerNum"),     // 채널 호스트 팔로우 숫자
                                    followObject.getInt("nowLive")           // 현재 방송 유무
                            ))
                        }
                    }
                }
                // 채널 데이터 전송이 완료된 후 List 들에 담은 뒤 RecyclerView 를 갱신한다.
                activity!!.runOnUiThread({
                    // 온라인 상태인 채널이 없는 경우 온라인 레이아웃을 숨긴다.
                    // 채널이 있는 경우 onlineRv 를 갱신한다.
                    if(onlineList.size != 0){ // 온라인 채널이 있는 경우
                        channelOnlineLo.visibility = View.VISIBLE // 온라인 레이아웃을 보이게한다.
                        onlineAdapter.updateChannel(onlineList) // RecyclerView 를 갱신한다.
                    }else{ // 온라인 채널이 없는 경우
                        channelOnlineLo.visibility = View.GONE // 온라인 레이아웃을 숨긴다.
                    }

                    // 오프라인 상태인 채널이 없는 경우 오프라인 레이아웃을 숨긴다.
                    // 채널이 있는 경우 offlineRv 를 갱신한다.
                    if(offlineList.size != 0){ // 오프라인 채널이 있는 경우
                        channelOfflineLo.visibility = View.VISIBLE // 오프라인 레이아웃을 보이게한다.
                        offlineAdapter.updateChannel(offlineList) // RecyclerView 를 갱신한다.
                    }else{  // 오프라인 채널이 없는 경우
                        channelOfflineLo.visibility = View.GONE // 오프라인 레이아웃을 숨긴다.
                    }

                    // 팔로우 리스트가 존재하지 않는 경우, 팔로우 한 채널이 없다는 메시지를 보여준다.
                    if(onlineList.size + offlineList.size > 0){ // 팔로우 한 채널이 있는 경우
                        channelNoneTv.visibility = View.GONE    // 채널이 없다는 메시지를 숨긴다.
                    }else{ // 팔로우 한 채널이 없는 경우
                        channelNoneTv.visibility = View.VISIBLE // 채널이 없다는 메시지를 보여준다..
                    }

                    if(channelSwipeLo != null){     // 스와이프 레이아웃이 설정되어 있는 경우
                        channelSwipeLo.isRefreshing = false     // 스와이프를 종료한다.
                    }
                    channelPb.visibility = View.GONE // 프로그레스 바를 숨긴다.
                })
            }catch (e : Exception){
                e.printStackTrace()
                activity!!.runOnUiThread({
                    if(channelSwipeLo != null){     // 스와이프 레이아웃이 설정되어 있는 경우
                        channelSwipeLo.isRefreshing = false     // 스와이프를 종료한다.
                    }
                    channelPb.visibility = View.GONE // 프로그레스 바를 숨긴다.
                })

            }
        }.start()
    }
}