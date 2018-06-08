package com.testexam.charlie.tlive.main.follow

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

import com.testexam.charlie.tlive.R

class FriendAdapter(private var friendList : ArrayList<User>, val context : Context) : RecyclerView.Adapter<FriendAdapter.FriendHolder>() {
    private val serverUrl = "http://13.125.64.135/profile/" // AWS 의 Elastic IP address

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend,parent,false)
        return FriendHolder(v)
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val user = friendList[position]

        // 친구 요청인 경우
        if(user.isRequest){
            holder.friendConfirmBtn.visibility = View.VISIBLE
            holder.friendNoBtn.visibility = View.VISIBLE

        // 이미 친구인 경우
        }else{
            holder.friendConfirmBtn.visibility = View.GONE
            holder.friendNoBtn.visibility = View.GONE
        }
        holder.friendNameTv.text = user.name // 이름 설정

        // 프로필 사진이 있는 경우
        // Glide 를 이용하여 프로필 사진 URL 을 friendProfileIv 에 넣는다.
        if(user.profileUrl !== "null"){
            Glide.with(context)
                    .load(serverUrl+user.profileUrl)
                    .into(holder.friendProfileIv)
        }
    }

    fun setDate(list : ArrayList<User>){
        friendList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
    class FriendHolder (friendHolder : View) : RecyclerView.ViewHolder(friendHolder){
        val friendProfileIv = friendHolder.findViewById<ImageView>(R.id.friendProfileIv)!!

        val friendNameTv = friendHolder.findViewById<TextView>(R.id.friendNameTv)!!
        val friendNickNameTv = friendHolder.findViewById<TextView>(R.id.friendNickNameTv)!!

        val friendConfirmBtn = friendHolder.findViewById<Button>(R.id.friendConfirmBtn)!!
        val friendNoBtn = friendHolder.findViewById<Button>(R.id.friendNoBtn)!!
    }
}