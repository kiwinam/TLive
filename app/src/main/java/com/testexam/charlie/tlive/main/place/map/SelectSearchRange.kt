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

class SelectSearchRange : BottomSheetDialogFragment(){

    private lateinit var rangeBar: RangeBar
    private lateinit var rangeTv : TextView
    private lateinit var rangeSelectBtn : Button

    private lateinit var rangeListener: RangeListener

    private val rangeText = arrayOf("100m","300m","500m","1km","3km")


    private var selectRange = 0
    companion object {
        fun newInstance(): SelectSearchRange = SelectSearchRange()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.bs_search_range, container, false)
        rangeBar = v.findViewById(R.id.rangeBar)
        rangeTv = v.findViewById(R.id.rangeTv)
        rangeSelectBtn = v.findViewById(R.id.rangeSelectBtn)

        //rangeBar.setOnRangeBarChangeListener
        rangeBar.setOnRangeBarChangeListener { _, _, rightPinIndex, _, _ ->
            rangeTv.text = rangeText[rightPinIndex]
            selectRange = rightPinIndex
        }

        rangeSelectBtn.setOnClickListener({
            rangeListener.changeLimitRange(rangeText[selectRange],selectRange)
            dismiss()
        })
        rangeBar.setRangePinsByIndices(0,selectRange)
        return v
    }

    fun setData(range : Double){
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

        //rangeBar.rightIndex = selectRange
    }

    fun setRangeListener(listener: RangeListener){
        rangeListener = listener

    }
}