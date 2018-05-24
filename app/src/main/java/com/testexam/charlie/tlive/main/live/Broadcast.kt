package com.testexam.charlie.tlive.main.live

/**
 * Created by charlie on 2018. 5. 24..
 */

data class Broadcast(val broadNo : Int,
                     val broadHostNo : Int,
                     val broadHostName : String,
                     val broadHostProfileUrl : String,
                     val broadRoomName : String,
                     val broadRoomTag : String,
                     val broadPreviewUrl : String,
                     val broadIsLive : Int
                     )