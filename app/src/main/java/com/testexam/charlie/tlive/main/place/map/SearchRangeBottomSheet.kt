package com.testexam.charlie.tlive.main.place.map

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.appyvet.materialrangebar.RangeBar
import com.testexam.charlie.tlive.R

/**
 * 맛집 검색 범위를 선택하는 바텀 시트
 *
 * 검색 범위를 선택하면 바텀 시트를 호출 한 Activity 에 변경된 범위를 전달한다.
 */
class SearchRangeBottomSheet : BottomSheetDialogFragment(){
    private lateinit var rangeBar: RangeBar     // 범위를 표시하는 RangeBar
    private lateinit var rangeTv : TextView     // RangeBar 값 변화에 따라 범위 글자를 보여주는 TextView
    private lateinit var rangeSelectBtn : Button    // 범위 선택 버튼

    private lateinit var rangeListener: RangeListener   // 바텀 시트를 호출한 Activity 에 범위 선택 결과를 리턴해주는 리스너

    private val rangeText = arrayOf("100m","300m","500m","1km","3km")   // 범위 선택 글자들의 배열
    private var selectRange = 0 // 범위 인덱스
    companion object {
        fun newInstance(): SearchRangeBottomSheet = SearchRangeBottomSheet()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.bs_search_range, container, false)
        rangeBar = v.findViewById(R.id.rangeBar)
        rangeTv = v.findViewById(R.id.rangeTv)
        rangeSelectBtn = v.findViewById(R.id.rangeSelectBtn)

        rangeBar.setRangePinsByIndices(0,selectRange)   // rangeBar 의 좌측은 사용하지 않도록 설정한다.

        // 범위 선택하는 RangeBar 에 변화를 감지하는 리스너를 설정한다.
        // 범위를 선택하면 해당 index 를 저장하고 글자를 표시한다.
        rangeBar.setOnRangeBarChangeListener { _, _, rightPinIndex, _, _ ->
            rangeTv.text = rangeText[rightPinIndex]     // 범위 선택 글자를 표시한다.
            selectRange = rightPinIndex     // 선택된 인덱스를 저장한다.
        }

        // 범위 선택 버튼
        // 버튼을 클릭하면 호출한 Activity 에 선택된 값을 전달한다.
        rangeSelectBtn.setOnClickListener({
            rangeListener.changeLimitRange(rangeText[selectRange],selectRange)      // 콜백 리스너를 통해 선택된 값을 전달한다.
            dismiss()       // 바텀 시트를 dismiss 한다.
        })
        return v
    }

    /*
     * 범위 선택 바텀 시트를 시작하기 전에 선택된 맛집 검색 범위를 받아온다.
     */
    fun setBeforeRange(range : Double){
        when(range){
            100.0->{
                selectRange = 0
            }
            300.0->{
                selectRange = 1
            }
            500.0->{
                selectRange = 2
            }
            1000.0->{
                selectRange = 3
            }
            3000.0->{
                selectRange = 4
            }
        }
    }

    /* 콜백 리스너를 등록한다. */
    fun setRangeListener(listener: RangeListener){
        rangeListener = listener
    }
}