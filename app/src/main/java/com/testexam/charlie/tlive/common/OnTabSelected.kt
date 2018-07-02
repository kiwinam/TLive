package com.testexam.charlie.tlive.common

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager


/**
 * 탭 레이아웃이 선택됐을 때 뷰 페이저를 이동하는 클래스
 */
class OnTabSelected(private val viewPager: ViewPager) : TabLayout.OnTabSelectedListener {
    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    /*
     * 탭 레이아웃이 선택 되었을 때 뷰 페이저도 같은 위치로 이동한다.
     */
    override fun onTabSelected(tab: TabLayout.Tab?) {
        viewPager.currentItem = tab!!.position  // 뷰 페이저의 위치를 탭의 위치로 변경한다.
    }
}