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

class MovieFragment : Fragment() {
    private lateinit var myBroadcastList : ArrayList<Broadcast>
    private lateinit var myBroadcastAdapter : BroadcastAdapter

    private var userEmail = ""

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_movie, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        userEmail = context!!.getSharedPreferences("login",Context.MODE_PRIVATE).getString("email","none")

        movieSwipeLo.setOnRefreshListener {
            getMyBroadcast()
        }

        setRecyclerView()
    }

    private fun setRecyclerView(){
        myBroadcastList = ArrayList()
        myBroadcastAdapter = BroadcastAdapter(myBroadcastList,context!!,fragmentManager!!)

        movieRv.adapter = myBroadcastAdapter
        movieRv.layoutManager = LinearLayoutManager(context!!)
        movieRv.isNestedScrollingEnabled = false

        getMyBroadcast()
    }


    private fun getMyBroadcast(){
        Thread(Runnable {
            try{
                activity!!.runOnUiThread({
                    moviePb.visibility = View.VISIBLE
                })
                val paramList = ArrayList<Params>()
                paramList.add(Params("userEmail",userEmail))
                val result = HttpTask("getMyBroadcast.php", paramList).execute().get()
                if(result != null){
                    val array = JSONArray(result)
                    myBroadcastList.clear()
                    for(i in 0 until array.length()){
                        val responseObject = array.getJSONObject(i)
                        myBroadcastList.add(Broadcast
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
                        myBroadcastAdapter.setData(myBroadcastList)
                        myBroadcastAdapter.notifyDataSetChanged()
                        if(myBroadcastList.size > 0){
                            movieNoneTv.visibility = View.GONE
                            movieRvTextTv.visibility = View.VISIBLE
                        }else{
                            movieNoneTv.visibility = View.VISIBLE
                            movieRvTextTv.visibility = View.GONE
                        }

                        if(movieSwipeLo != null){
                            movieSwipeLo.isRefreshing = false
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