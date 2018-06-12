package com.testexam.charlie.tlive.main.follow

import android.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.widget.*
import com.bumptech.glide.Glide

import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.main.follow.chat.ChatActivity
import com.testexam.charlie.tlive.main.follow.chat.ClickListener

class FriendAdapter(val email : String, private var friendList : ArrayList<User>,  val context : Context) : RecyclerView.Adapter<FriendAdapter.FriendHolder>() {
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
            holder.friendConfirmBtn.setOnClickListener({
                val dialog : AlertDialog.Builder = AlertDialog.Builder(context,R.style.myDialog)
                dialog.setTitle("친구 수락")
                        .setMessage(user.name+"님의 친구 요청을 수락하시겠습니까?")
                        .setPositiveButton("수락", { dialog, which ->
                            val params = ArrayList<Params>()
                            params.add(Params("email",email))
                            params.add(Params("requestNo",user.friendNo.toString()))
                            params.add(Params("answer","ok"))
                            val result = HttpTask("procRequestFriend.php",params).execute().get()
                            Log.d("result accept",result+"..")
                            if(result == "ok"){
                                Toast.makeText(context,"친구 요청을 수락했습니다.",Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context,"일시적인 에러가 발생했습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        })
                        .setNegativeButton("취소", { dialog, which -> dialog.cancel() })
                        .show()

            })
            holder.friendNoBtn.setOnClickListener({
                val dialog : AlertDialog.Builder = AlertDialog.Builder(context,R.style.myDialog)
                dialog.setTitle("거절")
                        .setMessage(user.name+"님의 친구 요청을 거절하시겠습니까?")
                        .setPositiveButton("거절", { dialog, which ->
                            val params = ArrayList<Params>()
                            params.add(Params("email",email))
                            params.add(Params("requestNo",user.friendNo.toString()))
                            params.add(Params("answer","no"))
                            val result = HttpTask("procRequestFriend.php",params).execute().get()
                            Log.d("result decline ",result+"..")
                            if(result == "ok"){
                                Toast.makeText(context,"친구 요청을 거절했습니다.",Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context,"일시적인 에러가 발생했습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        })
                        .setNegativeButton("취소", { dialog, which -> dialog.cancel() })
                        .show()

            })
//            holder.friendLo.setOnClickListener {
//                Log.d("friendHolder","friendLo clicked")
//                val chatIntent = Intent(context, ChatActivity::class.java)
//                chatIntent.putExtra("targetEmail",user.email)
//                chatIntent.putExtra("targetName",user.name)
//                context.startActivity(chatIntent)
//            }
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

        // 처음 채팅 방으로 이동하기 위해 전체 뷰에 클릭 리스너 달아야함
        val friendLo = friendHolder.findViewById<RelativeLayout>(R.id.friendLo)

        val friendProfileIv = friendHolder.findViewById<ImageView>(R.id.friendProfileIv)!!

        val friendNameTv = friendHolder.findViewById<TextView>(R.id.friendNameTv)!!
        val friendNickNameTv = friendHolder.findViewById<TextView>(R.id.friendNickNameTv)!!

        val friendConfirmBtn = friendHolder.findViewById<Button>(R.id.friendConfirmBtn)!!
        val friendNoBtn = friendHolder.findViewById<Button>(R.id.friendNoBtn)!!
    }
}