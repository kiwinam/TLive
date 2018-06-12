package com.testexam.charlie.tlive.main.follow.chat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.testexam.charlie.tlive.R

class RoomAdapter(private var roomList : ArrayList<Room>, val context : Context) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room,parent,false)
        return RoomViewHolder(v)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room : Room = roomList[position]

        holder.roomNameTv.text = room.targetName // 방 이름 설정
        holder.roomCurrentTv.text = room.currentChat // 최근 보낸 메시지 설정

        // 안읽은 메시지 숫자 설정
        // badgeCount 가 0 이라면 badge 를 숨기고
        // badgeCount 가 0 이 아니라면 badge 를 보여주고 숫자를 설정한다.
        if(room.badgeCount != 0){
            holder.roomBadgeTv.visibility = View.VISIBLE
            holder.roomBadgeTv.text = room.badgeCount.toString()
        }else{
            holder.roomBadgeTv.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    fun setDate(list : ArrayList<Room>){
        roomList = list
        notifyDataSetChanged()
    }
    class RoomViewHolder (roomHolder : View) : RecyclerView.ViewHolder(roomHolder){
        val roomNameTv = roomHolder.findViewById<TextView>(R.id.roomNameTv)
        val roomCurrentTv = roomHolder.findViewById<TextView>(R.id.roomCurrentTv)
        val roomBadgeTv = roomHolder.findViewById<TextView>(R.id.roomBadgeTv)
    }
}