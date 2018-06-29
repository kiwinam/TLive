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
 */
class LikeListFragment : Fragment() {
    private lateinit var likeBroadcastList : ArrayList<Broadcast>
    private lateinit var broadcastAdapter : BroadcastAdapter

    private var userEmail = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_like_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userEmail = context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none") // 유저의 이메일을 shared preference 에서 가져온다.

        setRecyclerView()
        getLikeList()
    }

    /*
     * 좋아요 RecyclerView 를 설정하는 메소드
     */
    private fun setRecyclerView(){
        likeBroadcastList = ArrayList() // 좋아요 리스트 초기화
        broadcastAdapter = BroadcastAdapter(likeBroadcastList, context!!,fragmentManager!!)

        likeListRv.layoutManager = LinearLayoutManager(context!!)
        likeListRv.adapter = broadcastAdapter
        likeListRv.isNestedScrollingEnabled = false

        likeListSwipeLo.setOnRefreshListener({
            getLikeList()
        })
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
                //Glide.get(context!!).clearDiskCache()
                val paramList = ArrayList<Params>()
                paramList.add(Params("userEmail",userEmail))
                val httpTask = HttpTask("getLikeList.php", paramList)
                val result = httpTask.execute().get()
                if(result != null){
                    val array = JSONArray(result)
                    likeBroadcastList.clear()
                    for(i in 0..(array.length()-1)){
                        val responseObject = array.getJSONObject(i)
                        likeBroadcastList.add(Broadcast
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
                                true, // 좋아요 누른 리스트라 무조건 true 로 가져온다.
                                false
                        ))
                    }
                    activity!!.runOnUiThread({
                        broadcastAdapter.setData(likeBroadcastList)
                        broadcastAdapter.notifyDataSetChanged()
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