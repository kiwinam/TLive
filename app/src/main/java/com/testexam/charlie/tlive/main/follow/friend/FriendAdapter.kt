package com.testexam.charlie.tlive.main.follow.friend

import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params

/**
 * 친구 목록을 보여줄 때 사용하는 Adapter
 *
 * FollowFriendFragment 에 사용된다.
 * 새로운 친구 요청 목록과 기존 친구 목록 요청에 사용되며 각각 보여주는 View 의 형태가 다르다.
 * 새로운 친구 요청 같은 경우 요청을 수락할지 거절할지 선택하는 버튼이 표시된다.
 * 기존 친구 목록은 요청 수락, 거절 버튼이 표시 되지 않는다.
 */
@Suppress("NAME_SHADOWING")
class FriendAdapter(val email : String, private var friendList : ArrayList<User>, val context : Context) : RecyclerView.Adapter<FriendAdapter.FriendHolder>() {
    private val serverUrl = "http://13.125.64.135/profile/" // AWS 의 Elastic IP address
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend,parent,false)
        return FriendHolder(v)
    }

    /*
     * User 객체에 있는 데이터를 ViewHolder 에 표시한다.
     */
    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val user = friendList[position]     // friendList 포지션에 맞게 User 객체를 가져온다.

        // 새로운 친구 요청인 경우
        if(user.isRequest){
            holder.friendConfirmBtn.visibility = View.VISIBLE   // 친구 요청 수락 버튼을 보이게한다.
            holder.friendNoBtn.visibility = View.VISIBLE        // 친구 요청 거절 버튼을 보이게한다.
            holder.friendConfirmBtn.setOnClickListener({    // 친구 요청 수락 버튼에 ClickListener 를 설정한다.
                val dialog : AlertDialog.Builder = AlertDialog.Builder(context,R.style.myDialog)    // 친구 요청 수락을 눌렀을 때 표시될 다이얼로그.
                dialog.setTitle("친구 수락")    // 다이얼로그의 타이틀
                        .setMessage(user.name+"님의 친구 요청을 수락하시겠습니까?")    // 다이얼로그의 메시지
                        .setPositiveButton("수락", { dialog, _ -> // 다이얼로그에서 수락 버튼을 눌렀을 때.
                            val params = ArrayList<Params>()    // 서버에 전송될 파라미터들을 담고 있는 ArrayList
                            params.add(Params("email",email))   // 현재 유저의 이메일을 파라미터에 추가한다.
                            params.add(Params("requestNo",user.friendNo.toString()))    // 새로운 친구의 user_no 을 파라미터에 추가한다.
                            params.add(Params("answer","ok"))   // 친구 요청 수락 응답을 파라미터에 추가한다.
                            val result = HttpTask("procRequestFriend.php",params).execute().get()   // 서버에 친구 요청을 수락하는 php 에 파라미터를 전송한다.
                            if(result == "ok"){ // 결과가 ok 라면
                                Toast.makeText(context,"친구 요청을 수락했습니다.",Toast.LENGTH_SHORT).show()  // 토스트 메시지로 친구 요청이 완료되었음을 알려준다.
                            }else{  // 결과가 ok 가 아니라면
                                Toast.makeText(context,"일시적인 에러가 발생했습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show() // 토스트 메시지로 친구 요청 수락이 실패했음을 알려준다.
                            }
                            dialog.dismiss()    // 다이얼로그를 사라지게한다.
                        })
                        .setNegativeButton("취소", { dialog, _ -> dialog.cancel() })  // 다이얼로그에서 취소 버튼을 누르면 다이얼로그를 사라지게한다.
                        .show()

            })
            holder.friendNoBtn.setOnClickListener({ // 친구 요청 거절 버튼에 ClickLister 를 설정한다.
                val dialog : AlertDialog.Builder = AlertDialog.Builder(context,R.style.myDialog)   // 친구 요청 거절을 눌렀을 때 나타날 다이얼로그
                dialog.setTitle("거절")   // 다이얼로그 타이틀
                        .setMessage(user.name+"님의 친구 요청을 거절하시겠습니까?")    // 다이얼로그 메시지
                        .setPositiveButton("거절", { dialog, _ ->     // 거절 버튼 눌렀을 때
                            val params = ArrayList<Params>()    // 서버에 전송될 파라미터들을 담고 있는 ArrayList
                            params.add(Params("email",email))   // 현재 유저의 이메일을 파라미터의 추가한다.
                            params.add(Params("requestNo",user.friendNo.toString()))    // 새로운 친구의 user_no 을 파라미터에 추가한다.
                            params.add(Params("answer","no"))   // 친구 요청 수락 응답을 파라미터에 추가한다.
                            val result = HttpTask("procRequestFriend.php",params).execute().get()   // 서버에 친구 요청을 거절하는 php 에 파라미터를 전송한다.
                            if(result == "ok"){
                                Toast.makeText(context,"친구 요청을 거절했습니다.",Toast.LENGTH_SHORT).show() // 토스트 메시지로 친구 요청 거절이 완료되었음을 알려준다.
                            }else{
                                Toast.makeText(context,"일시적인 에러가 발생했습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show() // 토스트 메시지로 친구 요청 거절이 실패했음을 알려준다.
                            }
                            dialog.dismiss()    // 다이얼로그를 사라지게한다.
                        })
                        .setNegativeButton("취소", { dialog, _ -> dialog.cancel() }) //  다이얼로그에서 취소 버튼을 누르면 다이얼로그를 사라지게한다.
                        .show()

            })
        // 이미 친구인 경우
        }else{
            holder.friendConfirmBtn.visibility = View.GONE
            holder.friendNoBtn.visibility = View.GONE
        }
        holder.friendNameTv.text = user.name // 이름 설정

        // 프로필 사진이 있는 경우
        // Glide 를 이용하여 프로필 사진 URL 을 friendProfileIv 에 넣는다.
        if(user.profileUrl !== "null"){
            Glide.with(context)
                    .load(serverUrl+user.profileUrl)
                    .into(holder.friendProfileIv)
        }
    }
    /* friendList 를 갱신하고 어댑터에 데이터 세트가 변경되었음을 알려준다.*/
    fun setDate(list : ArrayList<User>){
        friendList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return friendList.size
    }
    class FriendHolder (friendHolder : View) : RecyclerView.ViewHolder(friendHolder){

        // 처음 채팅 방으로 이동하기 위해 전체 뷰에 클릭 리스너 달아야함
        val friendLo = friendHolder.findViewById<RelativeLayout>(R.id.friendLo)!!

        val friendProfileIv = friendHolder.findViewById<ImageView>(R.id.friendProfileIv)!!

        val friendNameTv = friendHolder.findViewById<TextView>(R.id.friendNameTv)!!
        val friendNickNameTv = friendHolder.findViewById<TextView>(R.id.friendNickNameTv)!!

        val friendConfirmBtn = friendHolder.findViewById<Button>(R.id.friendConfirmBtn)!!
        val friendNoBtn = friendHolder.findViewById<Button>(R.id.friendNoBtn)!!
    }
}