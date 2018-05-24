package com.testexam.charlie.tlive.main.follow

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.ProfileFragment
import kotlinx.android.synthetic.main.fragment_follow.*

/**
 * 팔로우 프레그먼트
 * Created by charlie on 2018. 5. 24
 */
class FollowFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_follow,container,false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.channel_ko)))
        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.friend_ko)))
        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.hide_chat_ko)))
        followTabLo.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{

            override fun onTabSelected(tab: TabLayout.Tab?) {
                var fragment : Fragment
                when(tab?.position){
                    0-> fragment = FollowChannelFragment.newInstance()
                    1-> fragment = FollowFriendFragment.newInstance()
                    2-> fragment = FollowChatFragment.newInstance()
                    else->fragment = FollowChannelFragment.newInstance()
                }
                openFragment(fragment)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
        openFragment(FollowChannelFragment.newInstance())

        followProfileIv.setOnClickListener({
            openFragment(ProfileFragment.newInstance())
        })
    }

    companion object {
        fun newInstance(): FollowFragment = FollowFragment()
    }

    private fun openFragment(fragment: Fragment){
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.followContainer,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}