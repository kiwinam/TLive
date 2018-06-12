package com.testexam.charlie.tlive.main.follow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.R.id.*
import com.testexam.charlie.tlive.R.string.email
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.common.RecyclerItemClickListener
import com.testexam.charlie.tlive.main.follow.chat.ChatActivity
import com.testexam.charlie.tlive.main.follow.chat.ClickListener
import kotlinx.android.synthetic.main.fragment_follow_friend.*
import org.androidannotations.annotations.Click
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by charlie on 2018. 5. 24..
 */
class FollowFriendFragment : Fragment() , View.OnClickListener{

    private var friendList : ArrayList<User>? = null
    private var friendAdapter : FriendAdapter? = null

    private var friendNewList : ArrayList<User>? = null
    private var friendNewAdapter : FriendAdapter? = null

    private var email : String? = null
    companion object {
        fun newInstance(): FollowFriendFragment = FollowFriendFragment()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow_friend,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email = context!!.getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none")


        setFriendListRecyclerView() // 친구 리스트 RecyclerView 설정
        friendEmptyPlusBtn.setOnClickListener(this)
        friendPlusFAB.setOnClickListener(this)

    }

    private fun setFriendListRecyclerView(){
        val linearLayoutManager = LinearLayoutManager(context)

        // 새로운 친구 요청 RecyclerView 설정
        friendNewList = ArrayList()
        friendNewAdapter = FriendAdapter(email!!, friendNewList!!,context!!)
        friendNewRv.layoutManager = LinearLayoutManager(context)
        friendNewRv.adapter = friendNewAdapter
        friendNewRv.addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))

        // 기존 친구 리스트 RecyclerView 설정
        friendList = ArrayList()
        friendAdapter = FriendAdapter(email!!,friendList!!, context!!)
        friendListRv.layoutManager = LinearLayoutManager(context)
        friendListRv.adapter = friendAdapter
        friendListRv.addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))

        // recyclerView onClickListener
        friendListRv.addOnItemTouchListener(RecyclerItemClickListener(
                context, friendListRv, object : RecyclerItemClickListener.OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                val friend = friendList!!.get(position)
                Log.d("friend Rv", "onItemClick ($position)")
                val intent = Intent(context,ChatActivity::class.java)
                intent.putExtra("targetEmail",friend.email)
                intent.putExtra("targetName",friend.name)
                context!!.startActivity(intent)
            }

            override fun onLongItemClick(view: View?, position: Int) {
            }})
        )

        // 스와이프 레이아웃 설정
        friendSwipeLo.setOnRefreshListener({
            getFriendList()
        })

        getFriendList() // 친구 목록 불러오기
    }

    private fun getFriendList(){
        Thread{
            try{
                // 친구 목록 불러오기 전 기존 뷰들 GONE
                friendNewLo.visibility = View.GONE
                friendNewRv.visibility = View.GONE
                friendListLo.visibility = View.GONE
                friendListRv.visibility = View.GONE
                friendNestedScrollView.visibility = View.INVISIBLE
                friendEmptyLo.visibility = View.VISIBLE
                var isNewList = false
                var isFriendList = false

                friendNewList!!.clear()
                friendList!!.clear()

                val params : ArrayList<Params> = ArrayList()
                params.add(Params("email",email!!))
                val result = HttpTask("getFriendList.php",params).execute().get()
                if(result !== "none"){
                    val friendArray = JSONArray(result)
                    for(i in 0 .. (friendArray.length()-1)){
                        val friend : JSONObject = friendArray.getJSONObject(i)
                        Log.d("friend",friend.getString("email"))
                        // 친구 요청일 경우
                        if(friend.getInt("isRequest") == 1){
                            Log.d("friend","new request")
                            isNewList = true
                            friendNewList!!.add(User(
                                    friend.getString("email"),
                                    friend.getString("name"),
                                    friend.getString("profileUrl"), true,
                                    friend.getInt("friendNo")))
                        // 기존 친구 목록일 경우
                        }else{
                            Log.d("friend","already")
                            isFriendList = true
                            friendList!!.add(User(
                                    friend.getString("email"),
                                    friend.getString("name"),
                                    friend.getString("profileUrl"), false,
                                    friend.getInt("friendNo")))
                        }
                    }
                    // 친구 요청이 있는 경우
                    // 친구 요청 RecyclerView 와 TextView 를 Visible 한다.
                    if(isNewList){
                        friendNewLo.visibility = View.VISIBLE
                        friendNewRv.visibility = View.VISIBLE
                        friendEmptyLo.visibility = View.GONE
                    }
                    if(isFriendList){
                        friendListLo.visibility = View.VISIBLE
                        friendListRv.visibility = View.VISIBLE
                        friendEmptyLo.visibility = View.GONE
                    }
                    friendNestedScrollView.visibility = View.VISIBLE
                    activity!!.runOnUiThread({
                        friendNewAdapter!!.setDate(friendNewList!!)
                        friendAdapter!!.setDate(friendList!!)
                    })
                    Log.d("friendList",friendList!!.size.toString())
                }
            }catch (e : Exception){
                e.printStackTrace()
            }finally{
                if(friendSwipeLo != null){
                    friendSwipeLo.isRefreshing = false
                }
            }
        }.run()
    }

    override fun onClick(v: View?) {
        when(v){
            friendEmptyPlusBtn->{
                Log.d("friend","empty plus")
                startFindActivity()
            }
            friendPlusFAB -> {
                Log.d("friend","fab plus")
                startFindActivity()
                //startActivity(Intent(context,ChatActivity::class.java))
            }
        }
    }

    private fun startFindActivity(){
        val intent = Intent(context,FindFriendActivity::class.java)
        startActivity(intent)
        //activity!!.overridePendingTransition(R.anim.anim_slide_out_bottom,R.anim.anim_slide_in_bottom)
    }

}