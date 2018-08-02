package com.testexam.charlie.tlive.main.profile.modify.camera

import android.content.Context
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.testexam.charlie.tlive.main.profile.modify.camera.ui.GraphicOverlay

class GraphicFaceTrackerFactory(private var overlay: GraphicOverlay, private var position : Int, val context: Context, val mIsFrontFacing : Boolean) : MultiProcessor.Factory<Face> {
    override fun create(face: Face): Tracker<Face> {
        return GraphicFaceTracker(overlay, position, context,mIsFrontFacing)
    }
}