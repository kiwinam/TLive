package com.testexam.charlie.tlive.main.follow.chat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.testexam.charlie.tlive.R

/**
 * 채팅 방을 표시하는 RecyclerView 에서 사용하는 Adapter
 *
 * SQLite 에서 채팅 방 목록을 가져오고 FollowChatFragment 에 followChatRv 에 데이터를 표시한다.
 */
class RoomAdapter(private var roomList : ArrayList<Room>, val context : Context) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
    // item_chat_room 를 뷰로 만들고 RoomViewHolder 객체에 담아 리턴한다.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room,parent,false)
        return RoomViewHolder(v)
    }

    // 채팅 방 데이터를 ViewHolder 에 표시한다.
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room : Room = roomList[position]    // roomList 에 있는 Room 객체를 position 에 맞게 가져온다.

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

    /*
     * roomList 사이즈를 리턴한다.
     */
    override fun getItemCount(): Int {
        return roomList.size
    }

    /*
     * roomList 를 갱신하고 어댑터에 데이터 세트가 변경되었음을 알려준다.
     */
    fun setDate(list : ArrayList<Room>){
        roomList = list
        notifyDataSetChanged()
    }

    class RoomViewHolder (roomHolder : View) : RecyclerView.ViewHolder(roomHolder){
        val roomNameTv = roomHolder.findViewById<TextView>(R.id.roomNameTv)!!
        val roomCurrentTv = roomHolder.findViewById<TextView>(R.id.roomCurrentTv)!!
        val roomBadgeTv = roomHolder.findViewById<TextView>(R.id.roomBadgeTv)!!
    }
}