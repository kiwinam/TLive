package com.testexam.charlie.tlive.main.follow.friend

data class User(var email : String,
                var name : String,
                var profileUrl : String,
                val isRequest : Boolean,
                val friendNo : Int
                )

