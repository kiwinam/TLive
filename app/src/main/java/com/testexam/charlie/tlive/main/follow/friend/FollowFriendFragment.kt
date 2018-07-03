package com.testexam.charlie.tlive.main.follow.friend

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.common.RecyclerItemClickListener
import com.testexam.charlie.tlive.main.follow.chat.ChatActivity
import kotlinx.android.synthetic.main.fragment_follow_friend.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * 내 친구 목록을 보여주는 Fragment
 *
 * Fragment 가 만들어지면 서버에 새로운 친구 요청 목록과 기존 친구 목록을 보여준다.
 * 새로운 친구 요청의 경우 RecyclerView Item 우측에 요청 수락 , 거절 버튼이 나온다.
 * 요청을 수락할 경우 기존 친구 목록에 추가되고 거절할 경우 새로운 친구 요청 목록에서 삭제된다.
 * FAB 와 친구 목록이 없을 때 보여지는 버튼을 통해서 새로운 친구를 찾는 FindFriendActivity 로 이동할 수 있다.
 * Created by charlie on 2018. 5. 24..
 */
class FollowFriendFragment : Fragment() , View.OnClickListener{
    private lateinit var friendList : ArrayList<User>       // 기존 친구 목록
    private lateinit var friendAdapter : FriendAdapter      // 기존 친구 어댑터
    private lateinit var friendNewList : ArrayList<User>    // 새로운 친구 요청 목록
    private lateinit var friendNewAdapter : FriendAdapter   // 새로운 친구 요청 어댑터
    private var email : String = ""     // 현재 로그인한 유저의 이메일

    companion object {
        fun newInstance(): FollowFriendFragment = FollowFriendFragment()
    }
    /* fragment_follow_friend 레이아웃을 뷰로 만든다. */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow_friend,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // SharedPreference 에서 현재 로그인한 유저의 이메일 정보를 가져온다,
        email = context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none")

        setFriendListRecyclerView() // 친구 리스트 RecyclerView 설정
        friendEmptyPlusBtn.setOnClickListener(this) // 친구 없음 버튼에 클릭 리스너를 OnClickListener 에서 오버라이드한 onClick 에 설정한다.
        friendPlusFAB.setOnClickListener(this)  // 친구 추가 버튼에 클릭 리스너를 OnClickListener 에서 오버라이드한 onClick 에 설정한다.
    }

    /*
     * 친구 목록 RecyclerView 설정
     *
     * 새로운 친구 요청 목록과 기존 친구 목록에 사용되는 RecyclerView 를 설정한다.
     * 설정이 끝난 뒤에 서버에 친구 목록을 요청한다.
     */
    private fun setFriendListRecyclerView(){
        // 새로운 친구 요청 RecyclerView 설정
        friendNewList = ArrayList()     // 새로운 친구 리스트를 초기화한다.
        friendNewAdapter = FriendAdapter(email, friendNewList, context!!)   // 새로운 친구 목록에서 사용되는 FriendAdapter
        val linearLayoutManager = LinearLayoutManager(context)  // 새로운 친구 목록에서 사용될 레이아웃 매니저
        friendNewRv.layoutManager = LinearLayoutManager(context)    // friendNewRv 레이아웃 매니저를 설정한다.
        friendNewRv.adapter = friendNewAdapter  // friendNewRv 에 어댑터를 설정한다.
        friendNewRv.addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))  // friendNewRv 에 구분선을 추가한다.

        // 기존 친구 리스트 RecyclerView 설정
        friendList = ArrayList()        // 기존 친구 리스트를 초기화한다.
        friendAdapter = FriendAdapter(email, friendList, context!!) // 기존 친구 목록에서 사용되는 FriendAdapter
        friendListRv.layoutManager = LinearLayoutManager(context)   // friendListRv 에 레이아웃 매니저를 설정한다.
        friendListRv.adapter = friendAdapter        // friendListRv 에 어댑터를 설정한다.
        friendListRv.addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation)) // friendListRv 에 구분선을 추가한다.

        // recyclerView 에 onClickListener 를 설정한다.
        // friendListRv 를 클릭 했을 때 선택한 친구와 1:1 채팅을 할 수 있는 ChatActivity 로 이동한다.
        friendListRv.addOnItemTouchListener(RecyclerItemClickListener(
                context, friendListRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                val friend = friendList[position]   // 클릭한 친구의 User 객체를 가져온다.
                val intent = Intent(context,ChatActivity::class.java)   // ChatActivity 로 이동할 Intent
                intent.putExtra("targetEmail",friend.email) // Intent 에 친구 이메일을 넣는다.
                intent.putExtra("targetName",friend.name)   // Intent 에 친구 이름을 넣는다.
                context!!.startActivity(intent) // ChatActivity 로 이동한다.
            }
            override fun onLongItemClick(view: View?, position: Int) {}
        }))

        // 스와이프 레이아웃 설정
        friendSwipeLo.setOnRefreshListener({    // 스와이프가 일어났을 때
            getFriendList()     // 서버에 친구 목록을 다시 요청한다.
        })

        // RecyclerView 설정이 끝나면 서버에 친구 목록을 요청한다.
        getFriendList() // 친구 목록 불러오기
    }

    /*
     * 서버에 친구 목록을 요청하는 메소드
     *
     * 서버에 나에게 새로운 친구 요청을 한 목록과 기존에 있는 친구 목록을 요청한다.
     * 요청한 결과를 가져와 새로운 친구 목록이나 기존 친구 목록이 없다면 해당 RecyclerView 의 Visibility 를 GONE 으로 설정한다.
     * 결과를 받은 후 각 Adapter 를 통해 데이터 세트가 변경 되었음을 알린다.
     */
    private fun getFriendList(){
        Thread{
            try{
                // 친구 목록 불러오기 전 기존 뷰들 GONE
                friendNewLo.visibility = View.GONE          // 새로운 친구 레이아웃을 안보이게 한다.
                friendNewRv.visibility = View.GONE          // 새로운 친구 RecyclerView 를 안보이게 한다.
                friendListLo.visibility = View.GONE         // 기존 친구 레이아웃을 안보이게한다.
                friendListRv.visibility = View.GONE         // 기존 친구 RecyclerView 를 안보이게 한다.
                friendNestedScrollView.visibility = View.INVISIBLE  // 내부 스크롤 뷰를 안보이게 한다.
                friendEmptyLo.visibility = View.VISIBLE     // 친구 목록이 없을 때 보여주는 레이아웃을 안보이게 한다.
                var isNewList = false       // 새로운 친구 목록이 있는지 여부를 확인하는 변수
                var isFriendList = false    // 기존 친구 목록이 있는지 여뷰를 확인하는 변수

                friendNewList.clear()   // 새로운 친구 리스트를 초기화한다.
                friendList.clear()      // 기존 친구 리스트를 초기화한다.

                val params : ArrayList<Params> = ArrayList()    // 파라미터들을 담고 있을 ArrayList
                params.add(Params("email",email))       // 파라미터에 현재 로그인한 유저의 이메일을 담는다.
                val result = HttpTask("getFriendList.php",params).execute().get()   // 서버에 친구 목록을 요청한다. 결과 값은 result 에 넣는다.
                if(result !== "none"){      // 결과가 있는 경우
                    val friendArray = JSONArray(result) // JSONArray 로 result 를 파싱한다.
                    for(i in 0 .. (friendArray.length()-1)){
                        val friend : JSONObject = friendArray.getJSONObject(i)
                        // 새로운 친구 요청일 경우
                        if(friend.getInt("isRequest") == 1){
                            isNewList = true    // 새로운 친구 목록이 있음으로 isNewList 를 true 로 변경한다.
                            friendNewList.add(User( // 새로운 친구 목록에 새로운 User 객체를 만들고 친구의 정보를 넣는다.
                                    friend.getString("email"),      // 친구 요청한 User 의 이메일
                                    friend.getString("name"),       // 친구 요청한 User 의 이름
                                    friend.getString("profileUrl"), // 친구 요청한 User 의 프로필 사진 경로
                                    true,
                                    friend.getInt("friendNo")))     // 친구 요청한 User 의 user_no
                        // 기존 친구 목록일 경우
                        }else{
                            isFriendList = true     // 기존 친구 목록이 있음으로 isFriendList 를 true 로 변경한다.
                            friendList.add(User(    // 기존 친구 목록에 새로운 User 객체를 만들고 친구의 정보를 넣는다.
                                    friend.getString("email"),      // 친구의 이메일
                                    friend.getString("name"),       // 친구의 이름
                                    friend.getString("profileUrl"), // 친구의 프로필 사진 경로
                                    false,
                                    friend.getInt("friendNo")))     // 친구의 user_no
                        }
                    }
                    // 새로운 친구 요청이 있는 경우
                    if(isNewList){  // 새로운 친구 요청 목록을 표시하는 레이아웃과 RecyclerView 를 보여준다.
                        friendNewLo.visibility = View.VISIBLE
                        friendNewRv.visibility = View.VISIBLE
                        friendEmptyLo.visibility = View.GONE    // 친구 목록이 없을 때 보여주는 레이아웃을 안보이게 한다.
                    }
                    // 기존 친구 목록이 있는 경우
                    if(isFriendList){ // 기존 친구 목록을 표시하는 레이아웃과 RecyclerView 를 보여준다.
                        friendListLo.visibility = View.VISIBLE
                        friendListRv.visibility = View.VISIBLE
                        friendEmptyLo.visibility = View.GONE    // 친구 목록이 없을 때 보여주는 레이아웃을 안보이게 한다.
                    }
                    friendNestedScrollView.visibility = View.VISIBLE
                    activity!!.runOnUiThread({
                        friendNewAdapter.setDate(friendNewList) // 새로운 친구 목록의 데이터 세트의 변경을 알려준다.
                        friendAdapter.setDate(friendList)       // 기존 친구 목록의 데이터 세트 변경을 알려준다.
                    })
                }
            }catch (e : Exception){
                e.printStackTrace()
            }finally{
                if(friendSwipeLo != null){
                    friendSwipeLo.isRefreshing = false  // SwipeLayout 의 리프레시를 종료한다.
                }
            }
        }.run()
    }

    /*
     * View Click 에 반응하는 메소드
     * 각 뷰의 아이디에 따라 반응한다.
     */
    override fun onClick(v: View?) {
        when(v){
            friendEmptyPlusBtn->{
                startFindActivity() // 친구 찾기 Activity 로 이동한다.
            }
            friendPlusFAB -> {
                startFindActivity() // 친구 찾기 Activity 로 이동한다.
            }
        }
    }

    /* 친구 찾기 Activity 로 이동하는 메소드 */
    private fun startFindActivity(){
        val intent = Intent(context, FindFriendActivity::class.java)
        startActivity(intent)
    }

}