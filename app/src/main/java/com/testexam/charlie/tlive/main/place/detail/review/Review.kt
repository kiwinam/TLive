package com.testexam.charlie.tlive.main.place.detail.review

/**
 * 맛집 상세보기와 리뷰 전체보기 액티비티에서 리뷰 데이터를 담고 있는 데이터 클래스
 */
data class Review (
                   val userEmail : String,      // 리뷰를 남긴 사람의 이메일
                   val userName : String,       // 이름
                   val userProfile : String,    // 프로필 사진
                   val userReviewCount : Int,   // 이 사람이 남긴 리뷰의 총 개수
                   val reviewText : String,     // 리뷰
                   val reviewPoint : Int,       // 남긴 별점
                   val photoArray : String,     // 사진 JSONArray
                   val uploadTime : String      // 올린 시간
                   )