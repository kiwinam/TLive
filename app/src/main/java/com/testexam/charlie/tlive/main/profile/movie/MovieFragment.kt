package com.testexam.charlie.tlive.main.profile.movie


import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.fragment_movie.*
import org.json.JSONArray

/**
 * 내가 업로드한 방송의 리스트를 보여주는 Fragment
 *
 * 서버에 현재 로그인한 유저의 이메일을 파라미터로 전달하고 내가 업로드한 방송의 리스트를 요청한다.
 */
class MovieFragment : Fragment() {
    private lateinit var myBroadcastList : ArrayList<Broadcast>     // 내가 업로드한 방송의 리스트
    private lateinit var myBroadcastAdapter : BroadcastAdapter      // 방송 어댑터

    private var userEmail = ""  // 현재 로그인한 사용자의 이메일

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_movie, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // SharedPreference 에서 현재 로그인한 유저의 이메일을 가져온다.
        userEmail = context!!.getSharedPreferences("login",Context.MODE_PRIVATE).getString("email","none")

        // 스와이프 레이아웃에 리프레시 리스너를 설정한다.
        movieSwipeLo.setOnRefreshListener {
            getMyBroadcast()    // 스와이프 리프레시가 발생하면 내가 업로드한 방송의 리스트를 요청한다.
        }
        setRecyclerView()   // 방송 리스트 RecyclerView 를 설정한다.
    }
    /* 방송 리스트 RecyclerView 를 설정한다. */
    private fun setRecyclerView(){
        myBroadcastList = ArrayList()   // 내 방송 리스트를 초기화한다.
        myBroadcastAdapter = BroadcastAdapter(myBroadcastList,context!!,fragmentManager!!)  // 방송 어댑터를 초기화한다.

        movieRv.adapter = myBroadcastAdapter
        movieRv.layoutManager = LinearLayoutManager(context!!)
        movieRv.isNestedScrollingEnabled = false

        getMyBroadcast()    // 서버에 내가 올린 방송의 리스트를 요청한다.
    }

    /*
     * 서버에 내가 올린 방송의 리스트를 요청하는 메소드
     * 파라미터에 현재 로그인한 사용자의 이메일을 넣는다.
     */
    private fun getMyBroadcast(){
        Thread(Runnable {
            try{
                activity!!.runOnUiThread({
                    moviePb.visibility = View.VISIBLE   // 프로그레스 바를 보이게한다.
                })
                val paramList = ArrayList<Params>()     // 파라미터를 담고 있는 ArrayList
                paramList.add(Params("userEmail",userEmail))    // userEmail 를 파라미터에 넣는다.
                val result = HttpTask("getMyBroadcast.php", paramList).execute().get()  // 서버에 내가 올린 방송의 리스트를 요청한다.
                if(result != null){ // 결과가 null 이 아니라면
                    val array = JSONArray(result)   // JSONArray 형식으로 변경한다.
                    myBroadcastList.clear() // 내 방송 리스트를 초기화한다.
                    for(i in 0 until array.length()){
                        val responseObject = array.getJSONObject(i)
                        myBroadcastList.add(Broadcast   // 내 방송 리스트에 새로운 Broadcast 객체를 추가한다.
                                (responseObject.getString("hostEmail"),         // BJ 이메일
                                (responseObject.getString("hostName")),         // BJ 이름
                                (responseObject.getString("hostProfileUrl")),   // BJ 프로필 경로
                                (responseObject.getInt("roomNo")),      // 방 번호
                                (responseObject.getInt("roomSessionNo")),   // WebRTC Node.js 상 세션 번호
                                (responseObject.getString("roomName")),     // 방 이름
                                (responseObject.getString("roomTag")),      // 방 태그
                                (responseObject.getInt("likeNum")),         // 좋아요 개수
                                (responseObject.getInt("viewerNum")),       // 시청자 수
                                (responseObject.getInt("isLive")),          // 현재 라이브 방송인지 아닌지 확인하는 변수, 1 = 라이브,  0 = VOD
                                (responseObject.getString("uploadTime")),   // 방송을 올린 시간
                                (responseObject.getString("previewSrc")),   // 미리보기 경로
                                (responseObject.getString("vodSrc")),       // VOD 경로
                                (responseObject.getBoolean("isLike")),      // 내가 이 방송을 좋아요 클릭했는지   // 0 = no, 1 = yes
                                (responseObject.getBoolean("isSubscribe"))  // 내가 이 방송의 BJ 를 구독 중인지   // 0 = no, 1 = yse
                        ))
                    }
                    activity!!.runOnUiThread({
                        myBroadcastAdapter.setBroadcastList(myBroadcastList)    // 방송 리스트를 업데이트한다.
                        if(myBroadcastList.size > 0){   // 내가 올린 방송이 하나라도 있으면
                            movieNoneTv.visibility = View.GONE
                            movieRvTextTv.visibility = View.VISIBLE
                        }else{
                            movieNoneTv.visibility = View.VISIBLE
                            movieRvTextTv.visibility = View.GONE
                        }

                        if(movieSwipeLo != null){
                            movieSwipeLo.isRefreshing = false   // 방송 정보를 가져오는 작업이 끝나면 스와이프 레이아웃을 안보이게한다.
                        }
                        moviePb.visibility = View.GONE
                    })
                }
            } catch (e : Exception){
                e.printStackTrace()
            }
        }).start()
    }
}