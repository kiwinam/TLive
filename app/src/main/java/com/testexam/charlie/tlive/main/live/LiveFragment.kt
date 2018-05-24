package com.testexam.charlie.tlive.main.live

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import kotlinx.android.synthetic.main.fragment_live.*

/**
 * 라이브 방송 프레그먼트
 *
 * Created by charlie on 2018. 5. 24
 */
class LiveFragment : Fragment() {
    private val broadcastList : ArrayList<Broadcast> = ArrayList()
    companion object {
        fun newInstance(): LiveFragment = LiveFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_live,container,false)
        //return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        addBroadcast()
        liveRv.layoutManager = LinearLayoutManager(context)
        liveRv.adapter = BroadcastAdapter(broadcastList, context)
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
    }
}