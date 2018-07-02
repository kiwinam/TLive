package com.testexam.charlie.tlive.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.Log

/**
 * 가로 RecyclerView 에서 smoothScrollToPosition 을 사용하기 위해 LinearLayoutManager 를 상속받는 SnapLayoutManager
 *
 * getHorizontalSnapPreference 를 override 하고 리턴 값을 SNAP_TO_START 변경하여 스크롤에 스냅이 걸릴 수 있게 한다.
 */
class SnapLayoutManager(val context : Context) : LinearLayoutManager(context) {
    @SuppressLint("LogNotTimber")
    override fun smoothScrollToPosition(recyclerView: RecyclerView?, state: RecyclerView.State?, position: Int) {
        val smoothScroller = object : LinearSmoothScroller(context){
            override fun getHorizontalSnapPreference(): Int {
                return SNAP_TO_START
            }

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF?
            = this@SnapLayoutManager.computeScrollVectorForPosition(targetPosition)
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
        Log.d("snapLayoutManager","position = $position")
    }
}