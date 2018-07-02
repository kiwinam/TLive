package com.testexam.charlie.tlive.main.follow.friend

import android.support.v7.widget.RecyclerView
import android.view.View
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params

/**
 * 친구 찾기 어댑터
 *
 * 친구 찾기 결과를 Recycler View 에 표시하는 Adapter
 */
class FindAdapter (private val myEmail : String, private var friendList : ArrayList<User>,private val context : Context) : RecyclerView.Adapter<FindAdapter.FindHolder>() {
    private val serverUrl = "http://13.125.64.135/profile/" // AWS 의 Elastic IP address
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_find_friend,parent,false)
        return FindHolder(v)
    }

    /* User 의 데이터를 ViewHolder 에 표시한다. */
    override fun onBindViewHolder(holder: FindHolder, position: Int) {
        val user : User = friendList[position]      // friendList 에서 Position 에 맞는 User 객체를 가져온다.

        holder.findFriendNameTv.text = user.name    // 이름을 표시한다.
        holder.findFriendPlusIv.setOnClickListener({    // 친구 추가 버튼 리스너
            Thread{
                val params = ArrayList<Params>()    // 파라미터 ArrayList
                params.add(Params("myEmail",myEmail))   // 나의 이메일을 파라미터에 추가한다.
                params.add(Params("targetEmail",user.email))    // 상대방의 이메일을 파라미터에 추가한다.
                val requestResult = HttpTask("requestFriend.php",params).execute().get()    // requestFriend.php 에 친구 요청을 보낸다. 결과 값은 requestResult 변수에 저장한다.
                if(requestResult == "ok"){  // 리턴 받은 결과 값이 ok 라면
                    Toast.makeText(context,user.name+"님에게 친구 요청을 보냈습니다.",Toast.LENGTH_SHORT).show() // 친구 추가 요청이 성공했다는 메시지를 보낸다.
                    holder.findFriendPlusIv.visibility = View.GONE  // 친구 추가 버튼을 안보이게한다.
                }else{
                    Toast.makeText(context,"친구 요청이 실패했습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()   // 친구 추가 요청이 실패했다는 메시지를 보낸다.
                }
            }.run()
        })
        if(user.profileUrl !== "null"){ // 유저의 프로필 경로가 있다면
            Glide.with(context) // 프로필 사진을 표시한다.
                    .load(serverUrl+user.profileUrl)
                    .into(holder.findFriendProfileIv)
        }
    }

    /* friendList 의 사이즈를 리턴한다. */
    override fun getItemCount(): Int {
        return friendList.size
    }

    /*
     * friendList 를 갱신하고 어댑터에 데이터 세트가 변경되었음을 알려준다.
     */
    fun setDate(list : ArrayList<User>){
        friendList = list
        notifyDataSetChanged()
    }

    class FindHolder (friendHolder : View) : RecyclerView.ViewHolder(friendHolder){
        val findFriendProfileIv = friendHolder.findViewById<ImageView>(R.id.findFriendProfileIv)!!
        val findFriendPlusIv = friendHolder.findViewById<ImageView>(R.id.findFriendPlusIv)!!

        val findFriendNameTv = friendHolder.findViewById<TextView>(R.id.findFriendNameTv)!!
    }
}