package com.testexam.charlie.tlive.main.place.write

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import kotlinx.android.synthetic.main.bs_review_close.*
import kotlinx.android.synthetic.main.bs_review_close.view.*


class CloseBottomSheet : BottomSheetDialogFragment(), View.OnClickListener{
    private lateinit var closeCallback: CloseCallback
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
            reviewCloseOk->{
                closeCallback.isClose(true)
                dismiss()
            }
            reviewCloseCancel->{
                closeCallback.isClose(false)
                dismiss()
            }
        }
    }

    fun setCallback(callback: CloseCallback){
        closeCallback = callback
    }
}