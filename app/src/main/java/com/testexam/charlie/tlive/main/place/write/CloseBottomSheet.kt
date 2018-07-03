package com.testexam.charlie.tlive.main.place.write

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import kotlinx.android.synthetic.main.bs_review_close.*
import kotlinx.android.synthetic.main.bs_review_close.view.*

/**
 * 리뷰 쓰기를 취소할 때 정말 취소할 건지 확인하는 바텀 시트
 */
class CloseBottomSheet : BottomSheetDialogFragment(), View.OnClickListener{
    private lateinit var closeCallback: CloseCallback   // 취소 유무를 전달하는 콜백 리스너
    companion object {
        fun newInstance(): CloseBottomSheet = CloseBottomSheet()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.bs_review_close, container, false)
        v.reviewCloseOk.setOnClickListener(this)
        v.reviewCloseCancel.setOnClickListener(this)
        return v
    }

    override fun onClick(v: View?) {
        when(v){
            reviewCloseOk->{    // 취소 버튼을 누르면
                closeCallback.isClose(true) // 콜백 리스너를 통해 취소한다는 뜻의 true 를 전달한다.
            }
            reviewCloseCancel->{    // 취소하지 않겠다는 버튼을 누르면
                closeCallback.isClose(false)    // 콜백 리스너를 통해 취소하지 않겠다는 뜻의 false 를 전달한다.
            }
        }
        dismiss()   // 바텀 시트를 dismiss 한다
    }
    /* 취소 유무를 전달하는 콜백 리스너를 설정한다. */
    fun setCallback(callback: CloseCallback){
        closeCallback = callback
    }
}