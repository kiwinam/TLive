package com.testexam.charlie.tlive.main.live.webrtc.broadChat

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.testexam.charlie.tlive.R

/**
 * 라이브 방송에서 사용되는 채팅 어댑터
 */
class ChatAdapter(private var chatLists : ArrayList<Chat>, val context : Context, private val colorMode : Int) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(){
    private val COLOR_VIEWER = 0    // 시청자가 보는 채팅 메시지
    private val COLOR_BROADCASTER = 1   // BJ 가 보는 채팅 메시지
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_broadcast_chat,parent,false)
        return ChatViewHolder(v)
    }
    /* 채팅 리스트의 사이즈를 리턴한다. */
    override fun getItemCount(): Int {
        return chatLists.size
    }

    /*
     * Chat 객체의 정보를 ViewHolder 에 표시한다.
     */
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatLists[position]  // chatList 에서 포지션에 맞는 Chat 객체를 가져온다.

        holder.broadChatSenderTv.text = chat.sender // 보낸 사람을 표시한다.
        holder.broadChatMessageTv.text = chat.message   // 메시지를 표시한다.
        if(colorMode == COLOR_VIEWER){  // 컬러 모드에 따라 채팅 글자의 색을 변경한다.
            holder.broadChatMessageTv.setTextColor(Color.BLACK)
        }else if(colorMode == COLOR_BROADCASTER){
            holder.broadChatMessageTv.setTextColor(Color.WHITE)
        }
    }

    /*
     * 채팅 리스트에 새로운 채팅 객체를 추가한다.
     */
    fun addChatItem(chat : Chat){
        chatLists.add(chat) // 채팅 리스트에 매개변수로 넘어온 Chat 객체를 추가한다.
        //notifyItemInserted(chatLists.size-1)
        notifyDataSetChanged() // 스레드 문제, UI Thread 에서 실행해야한다.
    }

    /*
     * 전체 채팅 리스트를 교체하는 메소드
     */
    fun setChatListData(newChatArray : ArrayList<Chat>){
        chatLists = newChatArray    // 매개 변수로 전달받은 newChatArray 를 chatList 에 넣는다.
        notifyDataSetChanged()  // 데이터 변경을 알린다.
    }

    /* 채팅 리스트를 초기화한다. */
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