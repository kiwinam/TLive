package com.testexam.charlie.tlive.main.profile

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.testexam.charlie.tlive.main.profile.like.LikeListFragment
import com.testexam.charlie.tlive.main.profile.movie.MovieFragment
import com.testexam.charlie.tlive.main.profile.review.ReviewListFragment
import com.testexam.charlie.tlive.main.profile.want.WantGoFragment

class ProfileFragmentAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        when(position){
            0-> return MovieFragment()
            1-> return WantGoFragment()
            2-> return LikeListFragment()
            3-> return ReviewListFragment()
        }
        return MovieFragment()
    }

    override fun getCount(): Int {
        return 4
    }

}