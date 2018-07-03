package com.testexam.charlie.tlive.main.live

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import kotlinx.android.synthetic.main.bs_subscribe.*

/**
 * 방송 목록 중에서 구독하기 버튼을 눌렀을 때 나타나는 바텀 시트
 *
 * 구독을 하거나 구독 취소를 할 수 있도록 한다.
 * 구독 버튼을 누른 방송의 호스트를 구독하고 있지 않다면 구독하기로 표시한다.
 * 구독 버튼을 누른 방송의 호스트를 이미 구독하고 있다면 구독 취소하기로 표시한다.
 *
 * 구독 버튼을 누르면 바텀 시트를 호출한 Fragment 콜백 메서드로 구독 버튼을 눌렀는지 매개변수로 전달한다.
 *
 */
class SubscribeBottomSheet : BottomSheetDialogFragment(), View.OnClickListener {
    private var userName = ""   //
    private var isSubscribe = false
    private lateinit var subscribeListener: SubscribeListener   // 구독 버튼 클릭할 때 호출될 콜백 리스너

    companion object {
        fun newInstance() : SubscribeBottomSheet { return SubscribeBottomSheet() }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bs_subscribe, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 구독하기 일때
        // 버튼의 텍스트를 구독하기로 변경한다.
        if(isSubscribe){
            subNameTv.text = "$userName 구독하기"
            subTextTv.text = "$userName 님을 구독하시겠습니까? 구독할 경우 $userName 님이 새로운 방송을 할 때마다 알림을 보내드립니다."
            subBtn.text = "구독하기"
        // 구독 취소일때
        // 버튼의 텍스트를 구독 취소하기로 변경한다.
        }else{
            subNameTv.text = "$userName 구독 취소하기"
            subTextTv.text = "$userName 님을 구독 취소하시겠습니까? 구독을 취소할 경우 더 이상 내 팔로우 리스트에서 보이지 않습니다."
            subBtn.text = "구독 취소하기"
        }
        setClickListeners() // 클릭 리스너를 설정한다.
    }

    /*
     * Fragment 에서 현재 누른 방송 호스트의 정보를 넘겨받는다.
     */
    fun setUserData(isSubscribe : Boolean, name : String, listener : SubscribeListener){
        this.isSubscribe = isSubscribe  // 현재 구독 상태
        userName = name // 방송 호스트의 이름
        subscribeListener = listener    // 콜백 리스너
    }

    /* 클릭 리스너 */
    private fun setClickListeners(){
        subCloseIv.setOnClickListener(this)
        subBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            subCloseIv->dismiss()   // 닫기 버튼을 누르면 바텀 시트를 dismiss 한다.
            subBtn->{
                subscribeListener.subscribe(isSubscribe)    // subBtn 을 누르면 변경된 구독 상태를 프레그먼트에 전달한다
                dismiss()
            }
        }
    }
}