package com.testexam.charlie.tlive.main.follow.chat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.DBHelper
import com.testexam.charlie.tlive.common.RecyclerItemClickListener
import kotlinx.android.synthetic.main.fragment_follow_chat.*
import kotlinx.android.synthetic.main.fragment_follow_friend.*

/**
 * 채팅 방 리스트를 보여주는 Fragment
 *
 * SQLite 에서 채팅 방 리스트를 가져온다.
 * 가져온 리스트를 roomList 에 넣고 RoomAdapter 를 통해 RecyclerView 에 채팅 방 리스트를 표시한다.
 *
 * 채팅 서비스에 바인드하여 새로운 채팅이 오면 가장 최근 나눈 채팅과 받은 시간을 변경한다.
 * Created by charlie on 2018. 5. 24..
 */
class FollowChatFragment : Fragment(){
    private lateinit var roomList : ArrayList<Room>     // 채팅 방 리스트를 가지고 있는 ArrayList
    private lateinit var roomAdapter : RoomAdapter      // followChatRv 에 어댑터
    private lateinit var dbHelper : DBHelper            // SQLite 에 연결하는 DBHelper 객체
    private var email : String = ""                     // 현재 로그인한 유저의 이메일
    private var isBound = false                          // 채팅 서비스와 바인드 유무를 저장하는 변수
    private lateinit var chatService : ChatService      // 채팅 서비스 객체

    companion object {
        fun newInstance(): FollowChatFragment = FollowChatFragment()
    }

    /*
     * 채팅 서비스와 바인드
     *
     * 채팅 서비스에 바인드한다. 바인드가 성공하면 콜백 메서드를 등록하고 isBound 변수를 true 로 변경한다.
     */
    private val chatConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) { // 채팅 서비스에 바인딩 되었을 때
            val chatBinder = service as ChatService.ChatBinder
            chatService = chatBinder.service        // 채팅 서비스를 가져온다.
            chatService.registerCallback(mCallback) // 콜백 메소드를 등록한다.
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {       // 채팅 서비스에 바인딩 실패 했을 때
            isBound = false     // 바인딩이 실패 했기 때문에 isBound 변수를 false 로 변경한다.
        }
    }

    /*
     *  콜백 함수 선언
     *
     *  채팅 서비스에서 새로운 메시지를 받을 때 바인딩된 Activity 나 Fragment 에 전달하기 위한 콜백 메소드
     */
    private val mCallback : ChatService.ReceiveCallback = object : ChatService.ReceiveCallback {
        override fun receiveMsg(msg: String?) {     // 새로운 메시지를 받았을 때
            getRoomList()       // 채팅 방 리스트를 새로 가져온다.
        }

        override fun checkNotification(targetEmail: String?): Boolean { // Notification 이 발생하는지 확인하는 메소드
            return false        // 방 목록에서는 무조건 Notification 이 발생하기 때문에 항상 false 를 리턴한다.
        }
    }

    /*
     * 채팅 서비스와 연결
     *
     * Fragment 가 onStart() 되었을 때 채팅 서비스에 바인드한다.
     */
    override fun onStart() {
        super.onStart()
        val bindIntent = Intent(context,ChatService::class.java)
        context!!.bindService(bindIntent,chatConnection,Context.BIND_AUTO_CREATE)
    }

    /*
     * 채팅 서비스와 연결 해제
     *
     * Fragment 가 onStop() 가 되었을 때 채팅 서비스를 언바인드 한다.
     */
    override fun onStop() {
        context!!.unbindService(chatConnection)
        isBound = false
        super.onStop()
    }

    // FollowChatFragment 뷰를 생성하고 리턴한다.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow_chat,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        email =context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email",null) // SharedPreference 에서 현재 로그인한 사용자의 이메일을 가져온다.

        dbHelper = DBHelper(context!!,email,null,1) // DBHelper 를 초기화한다.
        dbHelper.chatDB()       // SQLite 를 쓰기 가능한 상태로 변경한다.

        setChatRoomRecyclerView()       // 채팅 방 RecyclerView 를 설정한다.

        // 스와이프 리프레시 레이아웃 설정
        // 스와이프 시 방 목록을 다시 불러옴
        followChatSwipeLo.setOnRefreshListener {
            getRoomList()       // 방 목록을 불러오는 메소드 호출
        }
    }

    /*
     * 방 목록 RecyclerView (followChatRv) 를 설정한다.
     *
     * 1. 방 목록을 가지고 있을 ArrayList 을 초기화 하고 방 목록을 가져온다.
     * 2. followChatRv 에 어댑터와 레이아웃 매니저를 설정한다.
     * 3. followChatRv 에 클릭 리스너를 추가한다.
     */
    private fun setChatRoomRecyclerView(){
        // 1. 방 목록을 가지고 있을 ArrayList 을 초기화 하고 방 목록을 가져온다.
        roomList = ArrayList()          // 방 목록 리스트를 초기화한다.
        roomList = dbHelper.chatRoomList    // DBHelper 에서 방 목록을 가져온다.
        roomAdapter = RoomAdapter(roomList,context!!)   // RoomAdapter 를 초기화한다.
        val linearLayoutManager = LinearLayoutManager(context)  // 레이아웃 매니저를 Linear 로 생성한다.
        val divider = DividerItemDecoration(context,linearLayoutManager.orientation)    // 기본 디바이더를 레이아웃 매니저에 추가한다.

        // 2. followChatRv 에 어댑터와 레이아웃 매니저를 설정한다.
        followChatRv.layoutManager = linearLayoutManager    // followChatRv 에 레이아웃 매니저를 설정한다.
        followChatRv.adapter = roomAdapter                  // followChatRv 에 어댑터를 설정한다.
        followChatRv.addItemDecoration(divider)             // followChatRv 에 구분선을 추가한다.
        followChatRv.addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))

        // 3. followChatRv 에 클릭 리스너를 추가한다.
        followChatRv.addOnItemTouchListener(RecyclerItemClickListener(
                context, friendListRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {      // RecyclerView 에서 방 아이템을 클릭 했을 때
                val room = roomList[position]   // 클릭한 포지션의 Room 객체를 가져온다.
                val intent = Intent(context, ChatActivity::class.java)  // ChatActivity 로 이동하는 Intent 를 초기화한다.
                intent.putExtra("targetEmail",room.targetEmail) // 인텐트 Extra 에 targetEmail 를 추가한다.
                intent.putExtra("targetName",room.targetName)   // // 인텐트 Extra 에 targetName 를 추가한다.
                context!!.startActivity(intent)     // ChatActivity 로 이동한다.
            }
            override fun onLongItemClick(view: View?, position: Int) { }})
        )
        getRoomList()   // 방 목록을 불러온다.
    }

    /*
     * SQLite 에서 채팅 방 리스트를 가져온다.
     */
    private fun getRoomList(){
        Thread{
            val getRoomList = dbHelper.chatRoomList
            roomList = getRoomList
            activity!!.runOnUiThread({
                roomAdapter.setDate(getRoomList)
                if(followChatSwipeLo != null){
                    followChatSwipeLo.isRefreshing = false
                }
            })

        }.run()
    }
}