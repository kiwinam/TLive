package com.testexam.charlie.tlive.common

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager

class OnTabSelected(val viewPager: ViewPager) : TabLayout.OnTabSelectedListener {
    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        viewPager.currentItem = tab!!.position
    }
}