package com.testexam.charlie.tlive.main.follow.chat

import android.support.v7.widget.RecyclerView
import android.view.View
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.testexam.charlie.tlive.R

/**
 * ChatActivity 에서 chatRv 에 채팅 내역을 표시하는 ChatAdapter
 *
 * 채팅 내역을 SQLite 에서 가져와 chatList 에 넣는다.
 * chatList 에 있는 데이터들을 chatRv 에 뿌려준다.
 */
class ChatAdapter(private val myEmail : String, private var chatList : ArrayList<Chat>, val context : Context) : RecyclerView.Adapter<ChatAdapter.ChatHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat,parent,false)    // 뷰 생성
        return ChatHolder(v)    // ChatHolder 클래스로 리턴한다.
    }

    /*
     * 아이템의 총 수를 가져오는 메소드
     * chatList 의 크기를 리턴한다.
     */
    override fun getItemCount(): Int {
        return chatList.size
    }

    /*
     * 채팅 데이터를 ViewHolder 에 바인드한다.
     */
    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val chat : Chat = chatList[position]        // chatList 에 있는 Chat 객체를 position 에 맞게 가져온다.

        // 내가 보낸 메시지라면
        if(chat.senderEmail == myEmail){
            holder.chatMyTv.visibility = View.VISIBLE       // 내 말풍선은 보이게한다.
            holder.chatOpTv.visibility = View.INVISIBLE     // 상대방 말풍선은 보이지 않게 한다.

            holder.chatMyTv.text = chat.msg     // 말풍선에 채팅 메시지를 넣는다.
        // 상대방이 보낸 메시지라면
        } else {
            holder.chatMyTv.visibility = View.INVISIBLE     // 내 말풍선은 보이지 않게 한다.
            holder.chatOpTv.visibility = View.VISIBLE       // 상대방 말풍선은 보이게한다.

            holder.chatOpTv.text = chat.msg     // 말풍선에 채팅 메시지를 넣는다.
        }
    }

    /*
     * 채팅 리스트를 업데이트 하는 메소드
     *
     * 1. 매개 변수로 전달된 리스트를 chatList 에 넣는다.
     * 2. 어댑터에 notifyDataSetChanged() 를 호출하여 데이터 세트가 변경되었음을 알려준다.
     */
    fun updateList(list : ArrayList<Chat>){
        chatList = list         //  1. 매개 변수로 전달된 리스트를 chatList 에 넣는다.
        notifyDataSetChanged()  //  2. 어댑터에 notifyDataSetChanged() 를 호출하여 데이터 세트가 변경되었음을 알려준다.
    }

    /**
     * 채팅 뷰홀더 클래스
     */
    class ChatHolder (chatHolder : View) : RecyclerView.ViewHolder(chatHolder){
        val chatMyTv = chatHolder.findViewById<TextView>(R.id.chatMyTv)!!   // 내 말풍선 TextView
        val chatOpTv = chatHolder.findViewById<TextView>(R.id.chatOpTv)!!   // 상대방 말풍선 TextView
    }
}