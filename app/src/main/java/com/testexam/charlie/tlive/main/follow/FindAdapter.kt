package com.testexam.charlie.tlive.main.follow

import android.support.v7.widget.RecyclerView
import android.view.View
import android.content.Context
import android.util.Log
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
 */
class FindAdapter (private val myEmail : String,private var friendList : ArrayList<User>, val context : Context) : RecyclerView.Adapter<FindAdapter.FindHolder>() {
    private val serverUrl = "http://13.125.64.135/profile/" // AWS 의 Elastic IP address

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_find_friend,parent,false)
        return FindHolder(v)
    }

    override fun onBindViewHolder(holder: FindHolder, position: Int) {
        val user : User = friendList[position]

        holder.findFriendNameTv.text = user.name
        holder.findFriendPlusIv.setOnClickListener({
            Thread{
                Log.d("plus",user.email)
                val params = ArrayList<Params>()
                params.add(Params("myEmail",myEmail))
                params.add(Params("targetEmail",user.email))
                val requestResult = HttpTask("requestFriend.php",params).execute().get()
                if(requestResult == "ok"){
                    Toast.makeText(context,user.name+"님에게 친구 요청을 보냈습니다.",Toast.LENGTH_SHORT).show()
                    holder.findFriendPlusIv.visibility = View.GONE
                }else{
                    Log.d("result",requestResult)
                    Toast.makeText(context,"친구 요청이 실패했습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                }
            }.run()
        })
        if(user.profileUrl !== "null"){
            Glide.with(context)
                    .load(serverUrl+user.profileUrl)
                    .into(holder.findFriendProfileIv)
        }
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

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