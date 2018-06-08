package com.testexam.charlie.tlive.main.follow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import kotlinx.android.synthetic.main.fragment_follow_friend.*
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
        // 새로운 친구 요청 RecyclerView 설정
        friendNewList = ArrayList()
        friendNewAdapter = FriendAdapter(friendNewList!!, context!!)
        friendNewRv.layoutManager = LinearLayoutManager(context)
        friendNewRv.adapter = friendNewAdapter

        // 기존 친구 리스트 RecyclerView 설정
        friendList = ArrayList()
        friendAdapter = FriendAdapter(friendList!!, context!!)
        friendListRv.layoutManager = LinearLayoutManager(context)
        friendListRv.adapter = friendAdapter


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
                                    friend.getString("profileUrl"), true))
                        // 기존 친구 목록일 경우
                        }else{
                            Log.d("friend","already")
                            isFriendList = true
                            friendList!!.add(User(
                                    friend.getString("email"),
                                    friend.getString("name"),
                                    friend.getString("profileUrl"), false))
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
            }
        }
    }

    private fun startFindActivity(){
        val intent = Intent(context,FindFriendActivity::class.java)
        startActivity(intent)
        //activity!!.overridePendingTransition(R.anim.anim_slide_out_bottom,R.anim.anim_slide_in_bottom)
    }

}