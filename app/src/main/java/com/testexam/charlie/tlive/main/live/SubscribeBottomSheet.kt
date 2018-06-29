package com.testexam.charlie.tlive.main.live

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import kotlinx.android.synthetic.main.bs_subscribe.*

class SubscribeBottomSheet : BottomSheetDialogFragment(), View.OnClickListener {

    private var userName = ""
    private var isSubscribe = false
    private lateinit var subscribeListener: SubscribeListener

    companion object {
        fun newInstance() : SubscribeBottomSheet { return SubscribeBottomSheet()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //val v = inflater.inflate(R.layout.bs_subscribe, container, false)




        return inflater.inflate(R.layout.bs_subscribe, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isSubscribe){ // 구독하기 일때
            subNameTv.text = "$userName 구독하기"
            subTextTv.text = "$userName 님을 구독하시겠습니까? 구독할 경우 $userName 님이 새로운 방송을 할 때마다 알림을 보내드립니다."
            subBtn.text = "구독하기"
        }else{ // 구독 취소일때
            subNameTv.text = "$userName 구독 취소하기"
            subTextTv.text = "$userName 님을 구독 취소하시겠습니까? 구독을 취소할 경우 더 이상 내 팔로우 리스트에서 보이지 않습니다."
            subBtn.text = "구독 취소하기"
        }

        setClickListeners()
    }

    fun setUserData(isSubscribe : Boolean, name : String, listener : SubscribeListener){
        this.isSubscribe = isSubscribe
        userName = name
        subscribeListener = listener
    }

    private fun setClickListeners(){
        subCloseIv.setOnClickListener(this)
        subBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            subCloseIv->dismiss()
            subBtn->{
                subscribeListener.subscribe(isSubscribe)
                dismiss()
            }
        }
    }
}