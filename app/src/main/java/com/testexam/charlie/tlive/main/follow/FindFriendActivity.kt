package com.testexam.charlie.tlive.main.follow

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import com.testexam.charlie.tlive.R
import com.testexam.charlie.tlive.common.BaseActivity
import com.testexam.charlie.tlive.common.HttpTask
import com.testexam.charlie.tlive.common.Params
import kotlinx.android.synthetic.main.activity_find_friend.*
import org.json.JSONArray


class FindFriendActivity : BaseActivity(), View.OnClickListener{
    private var email : String = ""
    private var searchList : ArrayList<User> = ArrayList()
    private var findAdapter : FindAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)
        findCloseIb.setOnClickListener(this)
        email = getSharedPreferences("login", Context.MODE_PRIVATE).getString("email","none")

        setFindListRv()
        setEditTextSearch()
    }

    private fun setFindListRv(){

        findAdapter = FindAdapter(email, searchList,this)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.isSmoothScrollbarEnabled = true

        val divider = DividerItemDecoration(this,linearLayoutManager.orientation)

        findRv.adapter = findAdapter
        findRv.layoutManager = linearLayoutManager
        findRv.addItemDecoration(divider)
    }

    private fun setEditTextSearch(){
        findSearchEt.setOnEditorActionListener { v, actionId, event ->
            when (actionId){
                EditorInfo.IME_ACTION_SEARCH->{
                    Log.d("Editor","IME_ACTION_SEARCH")
                    requestFind(v.text.toString())
                    true
                }
                else->{
                    Log.d("Editor","else")
                    false
                }
            }
        }

        findSearchEt.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if(!s.isNullOrEmpty()){
                    if (s!![(s.length- 1)] == '\n') requestFind(s.toString())
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
    }

    private fun requestFind(searchString : String){
        Log.d("requestFind",searchString)
        if(!searchString.isEmpty()){
            Thread{
                try{
                    findPb.visibility = View.VISIBLE
                    val params :ArrayList<Params> = ArrayList()
                    params.add(Params("query",searchString))
                    params.add(Params("myEmail",email))
                    val result = HttpTask("findFriend.php",params).execute().get()
                    if(result != "failed"){
                        searchList.clear()
                        val resultArray = JSONArray(result)
                        for(i in 0 .. (resultArray.length()-1)){
                            val friend = resultArray.getJSONObject(i)
                            searchList.add(User(
                                    friend.getString("email"),
                                    friend.getString("name"),
                                    friend.getString("profileUrl"),false,0))
                        }
                        this.runOnUiThread({
                            findAdapter!!.setDate(searchList)
                        })
                    }
                }catch(e : Exception){

                }finally {
                    findPb.visibility = View.GONE
                }
            }.run()
        }
    }

    override fun onClick(v: View?) {
        when(v){
            findCloseIb-> onBackPressed()
        }
    }
}

