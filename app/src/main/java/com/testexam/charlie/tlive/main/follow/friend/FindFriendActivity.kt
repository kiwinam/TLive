package com.testexam.charlie.tlive.main.follow.friend

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import kotlinx.android.synthetic.main.activity_find_friend.*
import org.json.JSONArray

/**
 * 친구 찾기 Activity
 *
 * 새로운 친구를 찾아 친구 추가를 할 수 있는 Activity
 * 서버에 이메일이나 이름을 검색하여 친구 리스트를 받는다.
 * 받은 친구 리스트를 RecyclerView 에 표시하고 친구 추가 버튼을 누르면 친구 추가 요청이 상대방에게 보내진다.
 */
class FindFriendActivity : BaseActivity(){
    private var email : String = ""         // 현재 로그인한 유저의 이메일
    private lateinit var searchList : ArrayList<User>   // 친구 검색 결과가 들어가는 ArrayList
    private lateinit var findAdapter : FindAdapter      // findRv 에 사용되는 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)
        findCloseIb.setOnClickListener({ onBackPressed() }) // 닫기 버튼을 누르면 onBackPressed 메소드를 호출해서 Activity 를 종료한다.
        email = getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none")   // SharedPreference 에서 로그인한 유저의 이메일을 가져온다.
        setFindListRv()         // 친구 검색 결과가 표시되는 RecyclerView 설정
        setEditTextSearch()     // 검색 EditText 의 IME 버튼을 SEARCH 로 설정
    }

    /*
     * 친구 검색 결과 RecyclerView 를 설정하는 메소드
     */
    private fun setFindListRv(){
        findAdapter = FindAdapter(email, searchList, this)      // 친구 검색 어댑터 초기화
        val linearLayoutManager = LinearLayoutManager(this)     // Linear 레이아웃 매니저 초기화
        linearLayoutManager.isSmoothScrollbarEnabled = true     // 부드럽게 스크롤 되는 옵션 true

        val divider = DividerItemDecoration(this,linearLayoutManager.orientation)   // 구분선 초기화

        findRv.adapter = findAdapter    // findRv 어댑터 설정
        findRv.layoutManager = linearLayoutManager  // findRv 레이아웃 매니저 설정
        findRv.addItemDecoration(divider)       // findRv 구분선 추가
    }

    /*
     * 친구 검색을 하는 EditText 의 IME 를 SEARCH 로 설정한다.
     * IME_ACTION_SEARCH 일 때 서버에 친구 검색 결과를 요청하는 requestFind 메소드를 호출한다.
     */
    private fun setEditTextSearch(){
        findSearchEt.setOnEditorActionListener { v, actionId, _ ->
            when (actionId){
                EditorInfo.IME_ACTION_SEARCH->{
                    requestFind(v.text.toString())  // 서버에 친구 검색 결과를 요청하는 requestFind 메소드를 호출한다.
                    true
                }
                else->{
                    false
                }
            }
        }

        findSearchEt.addTextChangedListener(object : TextWatcher{   // findSearchEt 에 텍스트 변경을 감지하는 리스너를 추가한다.
            override fun afterTextChanged(s: Editable?) {
                if(!s.isNullOrEmpty()){
                    if (s!![(s.length- 1)] == '\n') requestFind(s.toString())   // 사용자가 엔터키를 입력하면 서버에 친구 검색 결과를 요청하는 requestFind 메소드를 호출한다.
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    /*
     * 서버에 친구 검색 결과를 요청한다
     *
     * 매개 변수로 전달 받은 searchString 를 파라미터에 넣는다.
     * searchString 과 일치하거나 포함되는 이름이나 이메일이 결과로 리턴된다.
     * 리턴된 결과를 searchList 에 추가한다.
     */
    private fun requestFind(searchString : String){
        if(!searchString.isEmpty()){
            Thread{
                try{
                    findPb.visibility = View.VISIBLE            // 프로그레스 바를 표시한다.
                    val params :ArrayList<Params> = ArrayList() // 파라미터 ArrayList 를 초기화한다.
                    params.add(Params("query",searchString))    // 파라미터에 query 를 추가한다
                    params.add(Params("myEmail",email))         // 파라미터에 검색을 요청하는 유저의 이메일을 추가한다.
                    val result = HttpTask("findFriend.php",params).execute().get()      // 서버에 findFriend.php 에 친구 찾기를 요청하고 결과를 result 에 저장한다.
                    if(result != "failed"){ // 친구찾기가 성공했다면
                        searchList.clear()  // 검색 결과 리스트를 초기화한다.
                        val resultArray = JSONArray(result)
                        for(i in 0 .. (resultArray.length()-1)){
                            val friend = resultArray.getJSONObject(i)
                            searchList.add(User(    // 친구의 정보를 User 에 담아 searchList 에 추가한다.
                                    friend.getString("email"),
                                    friend.getString("name"),
                                    friend.getString("profileUrl"), false, 0))
                        }
                        this.runOnUiThread({
                            findAdapter.setDate(searchList) // 어댑터를 통해 RecyclerView 를 갱신한다.
                        })
                    }
                }catch(e : Exception){
                    e.printStackTrace()
                }finally {
                    findPb.visibility = View.GONE   // 프로그레스 바를 안보이게한다.
                }
            }.run()
        }
    }
}

