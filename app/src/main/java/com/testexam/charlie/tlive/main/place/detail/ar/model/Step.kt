package com.testexam.charlie.tlive.main.place.detail.ar.model

data class Step(val startLocation : StartLocation?,
                val endLocation: EndLocation?,
                val geometry : String?,
                val type : String?,
                val modifier : String?
                )