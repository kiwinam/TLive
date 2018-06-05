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
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.main.live.webrtc.broadcaster.BroadCasterActivity

import kotlinx.android.synthetic.main.fragment_live.*
import org.json.JSONArray

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_live,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setOnClickListeners() // 클릭 리스너 설정
        setRecyclerView() // 리사이클러뷰 설정
        getBroadcastList()
    }

    override fun onClick(v: View?) {
        when(v){
            // 새로운 라이브 방송 시작하기 버튼
            // 라이브 방송에 필요한 권한 (카메라, 마이크) 가 허용 되어 있다면 방송 화면으로
            // 아니라면 권한을 요구하는 화면으로 이동한다.
            liveNewBtn->{

                val micPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO)
                val cameraPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)

                if(micPermission == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED){
                    // 방송 시작에 필요한 모든 권한을 가지고 있다면
                    startActivity(Intent(context, BroadCasterActivity::class.java))
//                    val kotlinJava = KotlinJavaFunction()
//                    kotlinJava.goLive(context)
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
        adapter = BroadcastAdapter(broadcastList,context!!)
        liveRv.layoutManager = LinearLayoutManager(context)
        liveRv.adapter = adapter
        liveRv.isNestedScrollingEnabled = false

        liveSwipeRefreshLo.setOnRefreshListener({
            getBroadcastList()
        })


    }

    private fun setOnClickListeners(){
        liveNewBtn.setOnClickListener(this)
        liveNewTextLo.setOnClickListener(this)
        liveProfileIv.setOnClickListener(this)
    }

    private fun getBroadcastList(){
        //Glide.get(context!!).clearMemory()

        Thread(Runnable {
            try{
                //Glide.get(context!!).clearDiskCache()
                val httpTask = HttpTask("getBroadcastList.php", ArrayList<Params>())
                val result = httpTask.execute().get()
                val array = JSONArray(result)
                broadcastList.clear()
                for(i in 0..(array.length()-1)){
                    val responseObject = array.getJSONObject(i)
                    broadcastList.add(Broadcast
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
                            (responseObject.getString("vodSrc"))
                    ))
                }
                activity!!.runOnUiThread({
                    adapter?.setData(broadcastList)
                    adapter?.notifyDataSetChanged()
                    if(liveSwipeRefreshLo != null){
                        liveSwipeRefreshLo.isRefreshing = false
                    }
                })
                
            } catch (e : Exception){
                e.printStackTrace()
            }
        }).start()
    }
}