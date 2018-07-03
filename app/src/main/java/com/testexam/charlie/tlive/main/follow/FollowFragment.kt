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
 *
 * 내가 구독한 채널, 친구 목록, 1:1 대화 목록을 표시하는 Fragment
 * TabLayout 에 채널, 친구, 귓속말 탭을 추가하고 각 탭을 누를때마다 Fragment 을 전환한다.
 * Created by charlie on 2018. 5. 24
 */
class FollowFragment : Fragment() {
    companion object {
        fun newInstance(): FollowFragment = FollowFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_follow,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // TabLayout 설정
        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.channel_ko)))    // 채널을 탭에 추가한다.
        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.friend_ko)))    // 친구를 탭에 추가한다.
        followTabLo.addTab(followTabLo.newTab().setText(getString(R.string.hide_chat_ko)))  // 귓속말을 탭에 추가한다.
        followTabLo.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{  // 탭을 선택할 때 반응할 Listener 설정
            // 탭이 선택 되었을 때
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val fragment : Fragment = when(tab?.position){  // 탭의 포지션을 가져온다.
                    0-> FollowChannelFragment.newInstance()     // 탭의 포지션이 0 이라면 ChannelFragment
                    1-> FollowFriendFragment.newInstance()      // 탭의 포지션이 1 이라면 FriendFragment
                    2-> FollowChatFragment.newInstance()        // 탭의 포지션이 2 라면 ChatFragment 로 변경한다.
                    else-> FollowChannelFragment.newInstance()
                }
                openFragment(fragment)
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
        openFragment(FollowChannelFragment.newInstance())   // 컨테이너가 담고 있는 프레그 먼트를 변경한다.

        followProfileIv.setOnClickListener({    // 프로필 버튼을 눌렀을 때
            startActivity(Intent(context,ProfileActivity::class.java))  // 프로필 액티비티로 이동한다.
        })
    }

    /*
     * 컨테이너에 표시할 Fragment 를 변경하는 메소드
     *
     * 매개변수로 전달 받은 Fragment 객체를 followContainer 에 표시한다.
     */
    private fun openFragment(fragment: Fragment){
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.followContainer,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}