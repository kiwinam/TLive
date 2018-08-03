package com.testexam.charlie.tlive.main.profile.modify.camera

import android.content.Context
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.testexam.charlie.tlive.main.profile.modify.camera.ui.GraphicOverlay

class GraphicFaceTracker internal constructor(private val mOverlay: GraphicOverlay, val position : Int, val context : Context, val mIsFrontFacing: Boolean) : Tracker<Face>() {
    private val mFaceGraphic : FaceGraphic = FaceGraphic(mOverlay, position, context,mIsFrontFacing)

    override fun onNewItem(faceId : Int, item : Face?) { }

    override fun onUpdate(p0: Detector.Detections<Face>?, face : Face?) {
        mOverlay.add(mFaceGraphic)
        face?.let { mFaceGraphic.updateFace(it) }
    }

    override fun onMissing(p0: Detector.Detections<Face>?) {
        mOverlay.remove(mFaceGraphic)
    }

    override fun onDone() {
        mOverlay.remove(mFaceGraphic)
    }
}