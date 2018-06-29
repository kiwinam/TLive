package com.testexam.charlie.tlive.main.follow

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.main.follow.channel.FollowChannelFragment
import com.testexam.charlie.tlive.main.follow.chat.FollowChatFragment
import com.testexam.charlie.tlive.main.follow.friend.FollowFriendFragment
import com.testexam.charlie.tlive.main.profile.ProfileActivity
import kotlinx.android.synthetic.main.fragment_follow.*

/**
 * 팔로우 프레그먼트
 * Created by charlie on 2018. 5. 24
 */
class FollowFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.channel_ko)))
        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.friend_ko)))
        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.hide_chat_ko)))
        followTabLo.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment : Fragment = when(tab?.position){
                    0-> FollowChannelFragment.newInstance()
                    1-> FollowFriendFragment.newInstance()
                    2-> FollowChatFragment.newInstance()
                    else-> FollowChannelFragment.newInstance()
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
            //openFragment(ProfileFragment.newInstance())
            startActivity(Intent(context,ProfileActivity::class.java))
        })
    }

    companion object {
        fun newInstance(): FollowFragment = FollowFragment()
    }

    private fun openFragment(fragment: Fragment){
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.followContainer,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}