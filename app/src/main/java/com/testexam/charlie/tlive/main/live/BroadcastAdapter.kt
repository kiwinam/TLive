package com.testexam.charlie.tlive.main.live


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.testexam.charlie.tlive.R

/**
 *
 * Created by charlie on 2018. 5. 24
 */
class BroadcastAdapter(private val broadcastList : ArrayList<Broadcast>, context : Context) : RecyclerView.Adapter<BroadcastAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.item_broadcast_card,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val broadcast = broadcastList[position]
        holder?.broadHostNameTv?.text = broadcast.broadHostName
        holder?.broadRoomNameTv?.text = broadcast.broadRoomName
        holder?.broadRoomTagTv?.text = broadcast.broadRoomTag
        if(broadcast.broadIsLive == 1){
            holder?.broadLiveTv?.visibility = View.VISIBLE
            holder?.broadVodTv?.visibility = View.GONE
        }else{
            holder?.broadLiveTv?.visibility = View.GONE
            holder?.broadVodTv?.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return broadcastList.size
    }

    class ViewHolder (broadView : View) : RecyclerView.ViewHolder(broadView){
        val broadPreviewIv = broadView.findViewById<ImageView>(R.id.broadPreviewIv)
        val broadHostIv = broadView.findViewById<ImageView>(R.id.broadHostIv)
        val broadMoreInfoIv = broadView.findViewById<ImageView>(R.id.broadMoreInfoIv)


        val broadHostNameTv = broadView.findViewById<TextView>(R.id.broadHostNameTv)
        val broadRoomNameTv = broadView.findViewById<TextView>(R.id.broadRoomNameTv)
        val broadRoomTagTv = broadView.findViewById<TextView>(R.id.broadRoomTagTv)
        val broadLiveTv = broadView.findViewById<TextView>(R.id.broadLiveTv)
        val broadVodTv = broadView.findViewById<TextView>(R.id.broadVodTv)
    }
}