package com.testexam.charlie.tlive.main.follow.channel

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R

/**
 * 채널 리스트를 담당하는 ChannelAdapter
 *
 * FollowerChannel RecyclerView 에 서버에서 가져온 채널의 정보를 담아 Recycler view Item 을 그린다.
 */
class ChannelAdapter(private var channelList : ArrayList<Channel>, val context : Context) : RecyclerView.Adapter<ChannelAdapter.ChannelHolder>() {
    private val serverUrl = "http://13.125.64.135:80/profile/"

    // 뷰 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_channel,parent,false)
        return ChannelHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ChannelHolder, position: Int) {
        val channel = channelList[position] // channelList 에서 현재 position 의 Channel 객체를 가져온다.

        // 아이템에 현재 채널의 정보를 입력한다.
        holder.channelNameTv.text = channel.name // 이름을 설정한다.
        holder.channelFollowerNumTv.text = "팔로워 ${channel.followerNum}명" // 현재 채널에 팔로우 수를 설정한다.
        if(channel.profileSrc != "null"){       // 프로필 사진의 경로가 있다면 프로필 사진을 서버에서 불러온다.
            Glide.with(context)
                    .load(serverUrl+channel.profileSrc)
                    .into(holder.channelProfileIv)
        }else{                                  // 프로필 사진의 경로가 없다면 기본 프로필 사진을 불러온다.
            Glide.with(context)
                    .load(context.getDrawable(R.drawable.ic_profile_ex1))
                    .into(holder.channelProfileIv)
        }

        // 현재 방송 중이라면 초록색 사각형을 , 아니라면 회색 사각각형을 표시한다.
        if(channel.isLive == 1){ // 현재 방송중
            holder.channelIsLiveIv.background = context.getDrawable(R.drawable.sp_live_on)
        }else{  // 현재 방송중이 아님.
            holder.channelIsLiveIv.background = context.getDrawable(R.drawable.sp_live_off)
        }


    }

    // 채널 리스트의 사이즈를 리턴하는 메소드
    override fun getItemCount(): Int {
        return channelList.size
    }

    /*
     * 변경된 채널을 업데이트하는 메소드
     */
    fun updateChannel(channelList: ArrayList<Channel>){
        this.channelList = channelList
        notifyDataSetChanged()
    }
    class ChannelHolder (holder : View) : RecyclerView.ViewHolder(holder){
        val channelProfileIv = holder.findViewById<ImageView>(R.id.channelProfileIv)!!
        val channelIsLiveIv = holder.findViewById<ImageView>(R.id.channelIsLiveIv)!!

        val channelNameTv = holder.findViewById<TextView>(R.id.channelNameTv)!!
        val channelFollowerNumTv = holder.findViewById<TextView>(R.id.channelFollowerNumTv)!!

    }
}