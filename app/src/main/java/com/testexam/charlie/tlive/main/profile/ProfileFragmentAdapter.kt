package com.testexam.charlie.tlive.main.profile

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.testexam.charlie.tlive.main.profile.like.LikeListFragment
import com.testexam.charlie.tlive.main.profile.movie.MovieFragment
import com.testexam.charlie.tlive.main.profile.review.ReviewListFragment
import com.testexam.charlie.tlive.main.profile.want.WantGoFragment

/**
 * ProfileActivity 에서 사용하는 뷰 페이저의 어댑터
 */
class ProfileFragmentAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        when(position){
            0-> return MovieFragment()      // 동영상 프레그먼트
            1-> return WantGoFragment()     // 가고싶은 프레그먼트
            2-> return LikeListFragment()   // 좋아요 프레그먼트
            3-> return ReviewListFragment() // 리뷰 프레그먼트
        }
        return MovieFragment()
    }

    override fun getCount(): Int {
        return 4
    }

}