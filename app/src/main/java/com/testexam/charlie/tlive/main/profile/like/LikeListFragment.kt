package com.testexam.charlie.tlive.main.profile.like

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
import com.testexam.charlie.tlive.main.live.Broadcast
import com.testexam.charlie.tlive.main.live.BroadcastAdapter
import kotlinx.android.synthetic.main.fragment_like_list.*
import org.json.JSONArray

/**
 * 내가 좋아요 누른 방송의 리스트를 보여주는 Fragment
 *
 * 서버에 내가 좋아요를 누른 방송의 리스트를 요청한다. 파라미터엔 현재 로그인한 사용자의 이메일을 담는다.
 */
class LikeListFragment : Fragment() {
    private lateinit var likeBroadcastList : ArrayList<Broadcast>   // 좋아요 누른 방송의 리스트
    private lateinit var broadcastAdapter : BroadcastAdapter        // 방송 리스트 어댑터

    private var userEmail = ""  // 현재 로그인한 사용자의 이메일
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_like_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 유저의 이메일을 shared preference 에서 가져온다.
        userEmail = context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none")
        setRecyclerView()   // 좋아요 누른 방송의 RecyclerView 를 설정한다.
    }

    /*
     * 좋아요 RecyclerView 를 설정하는 메소드
     */
    private fun setRecyclerView(){
        likeBroadcastList = ArrayList() // 좋아요 리스트 초기화
        broadcastAdapter = BroadcastAdapter(likeBroadcastList, context!!,fragmentManager!!) // 어댑터 초기화

        likeListRv.layoutManager = LinearLayoutManager(context!!)   // likeListRv 에 레이아웃 매니저를 설정한다.
        likeListRv.adapter = broadcastAdapter   // likeListRv 에 어댑터를 설정한다.
        likeListRv.isNestedScrollingEnabled = false // likeListRv 가 내부에 있어도 스크롤이 스무스하게 되도록 설정한다.

        // 스와이프 레이아웃의 리스너를 설정한다.
        likeListSwipeLo.setOnRefreshListener({
            getLikeList()   // 스와이프 하는 경우 서버에 좋아요 누른 방송의 리스트를 다시 요청한다.
        })
        getLikeList()   // RecyclerView 의 설정을 마치고 서버에 좋아요 누른 방송의 리스트를 요청한다.
    }

    /*
     * 내가 좋아요 누른 방송의 리스트를 가져오는 메소드
     */
    private fun getLikeList(){
        Thread(Runnable {
            try{
                activity!!.runOnUiThread({
                    //livePb.visibility = View.VISIBLE
                })
                val paramList = ArrayList<Params>()         // 파라미터를 담고 있는 ArrayList
                paramList.add(Params("userEmail",userEmail))    // userEmail 을 파라미터에 추가한다.
                val result = HttpTask("getLikeList.php", paramList).execute().get() // 서버에 내가 좋아요한 리스트를 요청한다. 결과를 result 에 저장한다.
                if(result != null){ // 결과가 있는 경우
                    val array = JSONArray(result)   // JSONArray 형식으로 result 값을 변환한다.
                    likeBroadcastList.clear()   // 좋아요 누른 방송 리스트를 초기화한다.
                    for(i in 0..(array.length()-1)){
                        val responseObject = array.getJSONObject(i)
                        likeBroadcastList.add(Broadcast // 방송 리스트에 새로운 Broadcast 객체를 추가한다.
                                (responseObject.getString("hostEmail"),         // 호스트의 이메일
                                (responseObject.getString("hostName")),         // 호스트의 이름
                                (responseObject.getString("hostProfileUrl")),   // 호스트의 프로필 경로
                                (responseObject.getInt("roomNo")),              // 방송의 방 번호
                                (responseObject.getInt("roomSessionNo")),       // node.js 상 세션 번호
                                (responseObject.getString("roomName")),         // 방의 이름
                                (responseObject.getString("roomTag")),          // 방의 태그
                                (responseObject.getInt("likeNum")),             // 좋아요 눌린 개수
                                (responseObject.getInt("viewerNum")),           // 시청자의 수
                                (responseObject.getInt("isLive")),              // 현재 라이브 방송 중인지 유무
                                (responseObject.getString("uploadTime")),       // 업로드한 시간
                                (responseObject.getString("previewSrc")),       // 미리보기 경로
                                (responseObject.getString("vodSrc")),           // VOD 경로
                                (responseObject.getBoolean("isLike")),          // 내가 좋아요 눌렀는지
                                (responseObject.getBoolean("isSubscribe"))      // 내가 구독하고 있는 호스트의 방송인지
                        ))
                    }
                    activity!!.runOnUiThread({
                        broadcastAdapter.setBroadcastList(likeBroadcastList)     // 어댑터에 있는 방송 리스트를 갱신한다.
                        if(likeListSwipeLo != null){
                            likeListSwipeLo.isRefreshing = false
                        }
                        //livePb.visibility = View.GONE
                        if(likeBroadcastList.size != 0){
                            likeListNoneTv.visibility = View.GONE
                        }else{
                            likeListNoneTv.visibility = View.VISIBLE
                        }
                    })
                }
            } catch (e : Exception){
                e.printStackTrace()
            }
        }).start()

    }
}