package com.testexam.charlie.tlive.main.follow.channel

data class Channel(val name : String,
                   val email : String,
                   val profileSrc : String,
                   val followerNum : Int,
                   val isLive : Int)