package com.testexam.charlie.tlive.main.follow.chat

import android.support.v7.widget.RecyclerView
import android.view.View
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.testexam.charlie.tlive.R

class ChatAdapter(private val myEmail : String, private var chatList : ArrayList<Chat>, val context : Context) : RecyclerView.Adapter<ChatAdapter.ChatHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat,parent,false)
        return ChatHolder(v)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val chat : Chat = chatList[position]

        // 내가 보낸 메시지라면
        if(chat.senderEmail == myEmail){

            holder.chatMyTv.visibility = View.VISIBLE
            holder.chatOpTv.visibility = View.INVISIBLE

            holder.chatMyTv.text = chat.msg
        // 상대방이 보낸 메시지라면
        } else {
            holder.chatMyTv.visibility = View.INVISIBLE
            holder.chatOpTv.visibility = View.VISIBLE

            holder.chatOpTv.text = chat.msg
        }
    }

    fun updateList(list : ArrayList<Chat>){
        chatList = list
        notifyDataSetChanged()
    }

    class ChatHolder (chatHolder : View) : RecyclerView.ViewHolder(chatHolder){
        val chatMyTv = chatHolder.findViewById<TextView>(R.id.chatMyTv)!!
        val chatOpTv = chatHolder.findViewById<TextView>(R.id.chatOpTv)!!
    }
}