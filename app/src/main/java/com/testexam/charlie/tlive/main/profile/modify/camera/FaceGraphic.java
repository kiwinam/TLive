package com.testexam.charlie.tlive.main.profile.modify.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import com.testexam.charlie.tlive.R;
import com.testexam.charlie.tlive.main.profile.modify.camera.ui.GraphicOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    private int maskPosition = 0;
    private int[] maskDrawable;
    private Context context;
    private boolean mIsFrontFacing;

    FaceGraphic(GraphicOverlay overlay, int maskPosition, Context context, boolean mIsFrontFacing) {
        super(overlay);

        this.maskPosition = maskPosition;
        this.context = context;
        maskDrawable = new int[]{R.drawable.dog, R.drawable.cat, R.drawable.iron, R.drawable.spider, R.drawable.batman, R.drawable.annony, R.drawable.submarine};
        this.mIsFrontFacing = mIsFrontFacing;


        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        /*List<Landmark> landmarks =  face.getLandmarks();
        Log.e("landmarks size",landmarks.size()+"..");*/
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        /*canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);*/

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        //canvas.drawRect(left, top, right, bottom, mBoxPaint);

        if(maskPosition != 0){
            Drawable d = ContextCompat.getDrawable(context, maskDrawable[maskPosition-1]);
            int l = (int) left;
            int t = (int) top;
            int r = (int) right;
            int b = (int) bottom;
            switch (maskPosition){
                case 1:     // 강아지
                    break;
                case 2:     // 고양지
                    break;
                case 3:     // 아이언맨
                    break;
                case 4:     // 스파이더맨
                    break;
                case 5:     // 배트맨
                    b = (int)(b/1.5);
                    break;
                case 6:     // 어나니머스
                    l -= 10;
                    r += 10;
                    break;


            }
            if(mIsFrontFacing){
                Objects.requireNonNull(d).setBounds(l,t,r,b);
            }else{
                Objects.requireNonNull(d).setBounds(r,t,l,b);
            }

            d.draw(canvas);
        }
    }
}
