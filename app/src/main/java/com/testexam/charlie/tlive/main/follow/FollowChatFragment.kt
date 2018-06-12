package com.testexam.charlie.tlive.main.follow

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.DBHelper
import com.testexam.charlie.tlive.common.RecyclerItemClickListener
import com.testexam.charlie.tlive.main.follow.chat.ChatActivity
import com.testexam.charlie.tlive.main.follow.chat.ChatService
import com.testexam.charlie.tlive.main.follow.chat.Room
import com.testexam.charlie.tlive.main.follow.chat.RoomAdapter
import kotlinx.android.synthetic.main.fragment_follow_chat.*
import kotlinx.android.synthetic.main.fragment_follow_friend.*

/**
 * Created by charlie on 2018. 5. 24..
 */
class FollowChatFragment : Fragment(){

    private var roomList : ArrayList<Room>? = null
    private var roomAdapter : RoomAdapter? = null

    private var dbHelper : DBHelper? = null
    private var email : String = ""

    private var isBound = false

    private var chatService : ChatService? = null

    companion object {
        fun newInstance(): FollowChatFragment = FollowChatFragment()
    }

    /*
     * 채팅 서비스와 바인드
     */
    private val chatConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            val chatBinder = service as ChatService.ChatBinder
            chatService = chatBinder.service
            chatService!!.registerCallback(mCallback)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBound = false
        }
    }

    /*
     *  콜백 함수 선언
     */
    private val mCallback : ChatService.ReceiveCallback = object : ChatService.ReceiveCallback {
        override fun receiveMsg(msg: String?) {
            getRoomList()
        }

        override fun checkNotification(targetEmail: String?): Boolean {
            return false
        }

    }

    /*
     * 채팅 서비스와 연결
     */
    override fun onStart() {
        super.onStart()
        val bindIntent = Intent(context,ChatService::class.java)
        context!!.bindService(bindIntent,chatConnection,Context.BIND_AUTO_CREATE)
    }

    /*
     * 채팅 서비스와 연결 해제
     */
    override fun onStop() {
        context!!.unbindService(chatConnection)
        isBound = false
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow_chat,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email =context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email",null)

        if(dbHelper == null){
            dbHelper = DBHelper(context!!,email,null,1)
            dbHelper!!.chatDB()
        }
        setChatRoomRecyclerView()

        // 스와이프 리프레시 레이아웃 설정
        // 스와이프 시 방 목록을 다시 불러옴
        followChatSwipeLo.setOnRefreshListener {
            getRoomList()
        }
    }

    private fun setChatRoomRecyclerView(){

        roomList = ArrayList()
        roomList = dbHelper!!.chatRoomList
        roomAdapter = RoomAdapter(roomList!!,context!!)
        val linearLayoutManager = LinearLayoutManager(context)
        val divider = DividerItemDecoration(context,linearLayoutManager.orientation)

        // RecyclerView 설정
        followChatRv.layoutManager = linearLayoutManager
        followChatRv.adapter = roomAdapter
        followChatRv.addItemDecoration(divider)

        followChatRv.addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))

        // recyclerView onClickListener
        followChatRv.addOnItemTouchListener(RecyclerItemClickListener(
                context, friendListRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                val room = roomList!![position]
                Log.d("friend Rv", "onItemClick ($position)")
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("targetEmail",room.targetEmail)
                intent.putExtra("targetName",room.targetName)
                context!!.startActivity(intent)
            }

            override fun onLongItemClick(view: View?, position: Int) {
            }})
        )
        getRoomList()
    }

    private fun getRoomList(){
        Thread{
            val getRoomList = dbHelper!!.chatRoomList
            roomList = getRoomList
            activity!!.runOnUiThread({
                roomAdapter!!.setDate(getRoomList)
                if(followChatSwipeLo != null){
                    followChatSwipeLo.isRefreshing = false
                }
            })

        }.run()
    }
}