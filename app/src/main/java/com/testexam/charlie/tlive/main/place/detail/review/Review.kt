package com.testexam.charlie.tlive.main.place.detail.review

data class Review (
                   val userEmail : String,
                   val userName : String,
                   val userProfile : String,
                   val userReviewCount : Int,
                   val reviewText : String,
                   val reviewPoint : Int,
                   val photoArray : String,
                   val uploadTime : String
                   )