package com.testexam.charlie.tlive.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * RecyclerView 클릭 리스너
 *
 * OnItemTouchListener 를 implements 해서 클릭 리스너로 사용한다.
 * Created by charlie on 2017. 11. 2
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;  // 클릭 리스너

    /*
     * OnItemClickListener 인터페이스
     *
     * 아이템이 클릭됐을 때와 길게 클릭됐을 때를 구별하여 구현한다.
     * SimpleOnGestureListener 에 onLongPress 를 오버라이딩 하여 길게 클릭 됐을 때 onLongItemClick 를 호출한다.
     */
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onLongItemClick(View view, int position);
    }

    private GestureDetector mGestureDetector;

    /*
     * RecyclerItemClickListener 생성자
     *
     * GestureDetector 를 생성하여 롱 클릭을 구현한다.
     */
    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener){
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(),e.getY());
                if(child != null && mListener != null){
                    mListener.onLongItemClick(child,recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    // onItemClick 메소드를 실제로 호출하는 onInterceptTouchEvent
    // RecyclerView 의 위치(x,y 좌표) 를 파악하여 현재 선택된 뷰를 찾는다.
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(),e.getY());
        if(childView != null && mListener != null && mGestureDetector.onTouchEvent(e)){
            mListener.onItemClick(childView, rv.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) { }
}
