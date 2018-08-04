package com.testexam.charlie.tlive.main.profile.modify.camera.mask

/**
 * 얼굴 마스크 객체
 * @param
 * name : 마스크 이름
 * isMask : 현재 Mask 객체가 마스크인지 마스크가 아닌지 (none 마스크)
 * isSelected : 마스크가 선택되었는지 확인하는 변수
 */
data class Mask(val name : String,
                val isMask : Boolean,
                var isSelected : Boolean
                ) {
}