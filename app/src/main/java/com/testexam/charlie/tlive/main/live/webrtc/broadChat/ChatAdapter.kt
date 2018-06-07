package com.testexam.charlie.tlive.main.live.webrtc.broadChat

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.testexam.charlie.tlive.R

class ChatAdapter(private var chatLists : ArrayList<Chat>, val context : Context, private val colorMode : Int) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(){
    private val COLOR_VIEWER = 0
    private val COLOR_BROADCASTER = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_broadcast_chat,parent,false)
        return ChatViewHolder(v)
    }

    override fun getItemCount(): Int {
        return chatLists.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatLists[position]

        holder.broadChatSenderTv.text = chat.sender
        holder.broadChatMessageTv.text = chat.message
        if(colorMode == COLOR_VIEWER){ //
            holder.broadChatMessageTv.setTextColor(Color.BLACK)
        }else if(colorMode == COLOR_BROADCASTER){
            holder.broadChatMessageTv.setTextColor(Color.WHITE)
        }
    }

    fun setData(chat : Chat){
        chatLists.add(chat)
        //notifyItemInserted(chatLists.size-1)
        notifyDataSetChanged() // 스레드 문제
    }

    fun setChatListData(newChatArray : ArrayList<Chat>){
        chatLists = newChatArray
        notifyDataSetChanged()
    }

    fun clearAll(){
        chatLists.clear()
        notifyDataSetChanged()
    }

//    fun setColorMode(colorMode : Int){
//        if(colorMode == COLOR_VIEWER){
//            holder.broadChatMessageTv.setTextColor(Color.BLACK)
//        }else if(colorMode == COLOR_BROADCASTER) {
//            holder.broadChatMessageTv.setTextColor(Color.WHITE)
//        }
//    }

    class ChatViewHolder(chatView : View) : RecyclerView.ViewHolder(chatView){
        val broadChatSenderTv = chatView.findViewById<TextView>(R.id.broadChatSenderTv)!!
        val broadChatMessageTv = chatView.findViewById<TextView>(R.id.broadChatMessageTv)!!
    }
}