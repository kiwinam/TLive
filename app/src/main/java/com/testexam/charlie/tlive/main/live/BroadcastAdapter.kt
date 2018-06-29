package com.testexam.charlie.tlive.main.live


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v4.app.FragmentManager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import com.testexam.charlie.tlive.main.live.webrtc.viewer.ViewerActivity
import com.testexam.charlie.tlive.main.live.webrtc.vod.VodActivity

/**
 * 라이브 방송 RecyclerView 의 Adapter
 *
 * 현재 실시간 스트리밍 중인 방송과 방송이 끝난 VOD 들의 리스트를 보여주는 어댑터이다.
 * 실시간 방송 중인 것과 VOD 에 따라 표시되는 View 의 모양이 다르다.
 * 라이브 방송을 클릭할 경우 WebRTC Socket 과 연결되는 ViewerActivity 로 이동하고,
 * VOD 를 클릭할 경우 VodActivity 로 이동하여 해당하는 vod 를 시청한다.
 *
 * Created by charlie on 2018. 5. 24
 */
class BroadcastAdapter(private var broadcastList : ArrayList<Broadcast>, val context : Context, private val fragmentManager : FragmentManager) : RecyclerView.Adapter<BroadcastAdapter.ViewHolder>()  {
    private val serverUrl = "http://13.125.64.135" // AWS 의 Elastic IP address
    private lateinit var scaleAnim : Animation
    private var userEmail = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_broadcast_card,parent,false)
        scaleAnim = AnimationUtils.loadAnimation(context,R.anim.scale_change_btn)
        userEmail = context.getSharedPreferences("login",Context.MODE_PRIVATE).getString("email","none")
        return ViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val broadcast = broadcastList[position]


        holder.broadHostNameTv.text = broadcast.hostName
        holder.broadRoomNameTv.text = broadcast.roomName
        holder.broadRoomTagTv.text = broadcast.roomTag
        holder.broadHostIv.setImageDrawable(context.getDrawable(R.drawable.ic_profile_ex1))

        /*
         * 라이브 스트리밍 방송일 경우,
         * 라이브 방송이라는 것을 알려주는 '생방송' 문구가 나오게 되고
         * 현재 시청하고 있는 인원을 알려준다.
         *
         * CardView 를 클릭하게 되면 현재 방송중인 Session 의 ID 를 가지고 ViewerActivity 로 이동하여 WebRTCPeerConnection 을 요청한다.
         */
        if(broadcast.isLive == 1){
            // 라이브 방송 상태를 알려주는 레이아웃을 보여준다.
            holder.broadLiveTv.visibility = View.VISIBLE
            holder.broadVodTv.visibility = View.GONE

            // CardView 클릭 리스너
            // CardView 를 클릭하게되면 현재 방송중인 Session 의 ID 를 가지고 ViewerActivity 로 이동한다.
            // ViewerActivity 에선 가져온 session Id 로 WebRTCPeerConnection 을 요청한다.
            holder.broadCardView.setOnClickListener( {
                val intent = Intent(context,ViewerActivity::class.java)
                intent.putExtra("presenterSessionId",broadcast.roomSessionNo)
                context.startActivity(intent)
            })

            // 현재 시청하고 있는 시청자의 수를 알려준다.
            holder.broadLiveNumTv.visibility = View.VISIBLE
            holder.broadLiveNumTv.text = "시청자"+broadcast.viewerNum.toString()+"명"
            holder.broadLiveCircle.visibility = View.VISIBLE

            // 라이브 방송의 미리보기를 제공한다. 미리보기는 3초마다 새로운 미리보기로 교체된다.
            Picasso.get().load(serverUrl+"/livePreview/"+broadcast.previewUrl).into(holder.broadPreviewIv)

        /*
         * VOD 일 경우,
         * VOD 일 경우 'VOD' 문구가 나오게 된다.
         *
         * CardView 를 클릭하게 되면 VodActivity 로 이동하게 되고, HLS 의 URL 를 Intent 로 넘긴다.
         * VodActivity 에선 전달받은 URL 로 VOD 를 재생한다.
         */
        }else{
            holder.broadLiveTv.visibility = View.GONE
            holder.broadVodTv.visibility = View.VISIBLE
            holder.broadCardView.setOnClickListener( {
                val intent = Intent(context,VodActivity::class.java)
                intent.putExtra("vodUrl",broadcast.vodUrl)
                context.startActivity(intent)
            })
            if(broadcast.previewUrl.endsWith("png")){
                Glide.with(context)
                        .load(serverUrl+"/preview/"+broadcast.previewUrl)
                        .into(holder.broadPreviewIv)
            }
            holder.broadLiveNumTv.visibility = View.GONE
            holder.broadLiveCircle.visibility = View.GONE

            // 좋아요를 아이콘을 초기화한다.
            // 좋아요가 눌렸다면 오렌지색의 하트를, 눌리지 않았다면 회색 선만 있는 하트를 broadLikeIv 에 넣는다.
            if(broadcast.isLike){
                Glide.with(context)
                        .load(context.getDrawable(R.drawable.ic_favorite_orange_24dp))
                        .into(holder.broadLikeIv)
            }else{
                Glide.with(context)
                        .load(context.getDrawable(R.drawable.ic_favorite_border_gray))
                        .into(holder.broadLikeIv)
            }
        }

        holder.broadLikeNumTv.text = broadcast.likeNum.toString()

        // 구독 버튼을 눌렀을 때,
        holder.broadSubscriptIv.setOnClickListener( {
            val willBeSubscribe = !broadcast.isSubscribe

            val subscribeBottomSheet = SubscribeBottomSheet.newInstance() // 구독하기 바텀 시트 객체를 초기화한다.
            subscribeBottomSheet.setUserData(willBeSubscribe,broadcast.hostName, object : SubscribeListener{ // 구독하기 바텀 시트에 초기 데이터와 콜백리스너를 설정한다.
                override fun subscribe(isSubscribe: Boolean) {
                    if(isSubscribe){ // 구독하기 버튼을 눌렀다면 서버에 변경할 데이터를 보낸다.
                        // 구독 아이콘을 오렌지 색으로 변경한다.
                        Glide.with(context)
                                .load(context.getDrawable(R.drawable.ic_subscriptions_orange_24dp))
                                .into(holder.broadSubscriptIv)
                    }else{
                        // 구독 아이콘을 회색으로 변경한다.
                        Glide.with(context)
                                .load(context.getDrawable(R.drawable.ic_subscriptions_gray_24dp))
                                .into(holder.broadSubscriptIv)
                    }
                    broadcast.isSubscribe = isSubscribe // 구독 상태를 true, false 로 변경한다.

                    // 서버에 변경된 구독 상태를 전송하는 Thread.
                    // subscribe.php 로 유저의 이메일, 구독 대상의 이메일, 구독 상태를 파라미터로 보낸다.
                    // 유저의 구독 리스트에서 추가하거나 제거한다.
                    // 리턴 값은 없다.
                    Thread{
                        val paramList = ArrayList<Params>() // 파라미터를 담을 ArrayList
                        paramList.add(Params("userEmail",userEmail)) // 유저 이메일을 파라미터에 추가한다.
                        paramList.add(Params("hostEmail",broadcast.hostEmail)) // 유저 이메일을 파라미터에 추가한다.
                        paramList.add(Params("isSubscribe",isSubscribe.toString())) // 유저 이메일을 파라미터에 추가한다.
                        HttpTask("subscribe.php",paramList).execute() // subscribe.php 로 전송하여 구독 상태를 변경한다.
                    }.start()
                }
            })
            subscribeBottomSheet.show(fragmentManager,"subBs")
        })

        // 좋아요 버튼을 눌렀을 때,
        holder.broadLikeIv.setOnClickListener({

            if(broadcast.isLike){ // 이미 좋아요 버튼이 눌려있다면
                // 안눌린 상태 값으로 변경한다.
                broadcast.isLike = false

                // 회색 라인만 있는 하트로 변경한다.
                Glide.with(context)
                        .load(context.getDrawable(R.drawable.ic_favorite_border_gray))
                        .into(holder.broadLikeIv)
                broadcast.likeNum -= 1 // 좋아요 개수 1 감소

            }else{ // 좋아요 버튼이 눌려있지 않다면
                // 눌린 상태로 변경한다.
                broadcast.isLike = true
                broadcast.likeNum += 1 // 좋아요 개수 1 증가


                // 오렌지색 하트로 변경한다.
                Glide.with(context)
                        .load(context.getDrawable(R.drawable.ic_favorite_orange_24dp))
                        .into(holder.broadLikeIv)

            }
            holder.broadLikeNumTv.text = broadcast.likeNum.toString() // 좋아요 개수 변경
            holder.broadLikeIv.startAnimation(scaleAnim) // 애니메이션 실행
            // 서버에 바뀐 상태값을 알려줘서 데이터 베이스를 변경하게 한다.
            Thread{
                val paramList = ArrayList<Params>()
                paramList.add(Params("userEmail",userEmail))
                paramList.add(Params("isLike",broadcast.isLike.toString()))
                paramList.add(Params("roomNo",broadcast.roomNo.toString()))
                HttpTask("broadLike.php",paramList).execute()
            }.start()
        })

    }

    override fun getItemCount(): Int {
        return broadcastList.size
    }

    fun setData(list : ArrayList<Broadcast>){
        broadcastList = list
    }

    class ViewHolder (broadView : View) : RecyclerView.ViewHolder(broadView){
        val broadCardView = broadView.findViewById<CardView>(R.id.broadCardView)!!

        val broadLiveCircle = broadView.findViewById<View>(R.id.broadLiveCircle)!!

        val broadPreviewIv = broadView.findViewById<ImageView>(R.id.broadPreviewIv)!!
        val broadHostIv = broadView.findViewById<ImageView>(R.id.broadHostIv)!!
        val broadMoreInfoIv = broadView.findViewById<ImageView>(R.id.broadMoreInfoIv)!!
        val broadLikeIv = broadView.findViewById<ImageView>(R.id.broadLikeIv)!!
        val broadSubscriptIv = broadView.findViewById<ImageView>(R.id.broadSubscriptIv)!!

        val broadHostNameTv = broadView.findViewById<TextView>(R.id.broadHostNameTv)!!
        val broadRoomNameTv = broadView.findViewById<TextView>(R.id.broadRoomNameTv)!!
        val broadRoomTagTv = broadView.findViewById<TextView>(R.id.broadRoomTagTv)!!
        val broadLiveTv = broadView.findViewById<TextView>(R.id.broadLiveTv)!!
        val broadVodTv = broadView.findViewById<TextView>(R.id.broadVodTv)!!
        val broadLiveNumTv = broadView.findViewById<TextView>(R.id.broadLiveNumTv)!!
        val broadLikeNumTv = broadView.findViewById<TextView>(R.id.broadLikeNumTv)!!

    }
}