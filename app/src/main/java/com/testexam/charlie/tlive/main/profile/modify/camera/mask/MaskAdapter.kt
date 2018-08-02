package com.testexam.charlie.tlive.main.profile.modify.camera.mask

import android.content.Context
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.testexam.charlie.tlive.R

/**
 * 마스크 어댑터
 */
class MaskAdapter (private var maskList : ArrayList<Mask>, val context: Context) : RecyclerView.Adapter<MaskAdapter.MaskHolder>() {
    private var maskDrawable = intArrayOf(R.drawable.dog, R.drawable.cat, R.drawable.iron, R.drawable.spider, R.drawable.batman, R.drawable.annony, R.drawable.submarine)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaskHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_mask,parent,false)
        return MaskHolder(v)
    }

    override fun getItemCount(): Int {
        return maskList.size
    }

    override fun onBindViewHolder(holder: MaskHolder, position: Int) {
        val mask = maskList[position]
        if(mask.isMask){    // 마스크를 선택했다면
            holder.maskNoneTv.visibility = View.GONE    // None 텍스트뷰를 안보이게한다.
            holder.maskIv.visibility = View.VISIBLE     // 마스크 이미지뷰를 보이게한다.

            holder.maskIv.setImageDrawable(ActivityCompat.getDrawable(context, maskDrawable[position-1]))   // 마스크 이미지뷰에 마스크 Drawable 를 넣는다.
        }else{  // 마스크 선택을 해제했다면
            holder.maskNoneTv.visibility = View.VISIBLE    // None 텍스트뷰를 안보이게한다.
            holder.maskIv.visibility = View.GONE     // 마스크 이미지뷰를 보이게한다.
        }

        if(mask.isSelected){        // 현재 선택된 마스크라면
            holder.maskSelectedRo.visibility = View.VISIBLE // 선택되었음을 알려주는 테두리 레이아웃을 보이게한다.
        }else{
            holder.maskSelectedRo.visibility = View.GONE    // 테두리 레이아웃을 숨긴다.
        }
    }

    /*
     * 마스크 선택시 테두리가 보이게 하는 메소드
     */
    fun setSelect(position : Int){
        maskList.forEach { it.isSelected = false }  // 전체 마스크 리스트의 선택을 false 로 초기화한다.
        maskList[position].isSelected = true    // 선택한 위치의 마스크의 선택된 상태를 true 로 변경한다.

        notifyDataSetChanged()  // 리사이클러 뷰에 변경된 사항에 대해 알림
    }

    class MaskHolder (maskHolder : View) : RecyclerView.ViewHolder(maskHolder){
        val maskNoneTv = maskHolder.findViewById<TextView>(R.id.maskNoneTv)!!

        val maskIv = maskHolder.findViewById<ImageView>(R.id.maskIv)!!

        val maskSelectedRo = maskHolder.findViewById<RelativeLayout>(R.id.maskSelectedRo)!!
    }
}