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

/**
 *  얼굴 인식 후 얼굴 위에 그래픽을 그리는 클래스
 *
 *  모바일 비전을 통해 인식된 얼굴의 위치에 마스크를 그린다.
 *  마스크는 총 7가지 종류가 있다.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private volatile Face mFace;    // 인식된 얼굴 객체

    private int maskPosition;       // 사용자가 선택한 마스크의 위치
    private int[] maskDrawable;     // 마스크 드로워블의 id 값을 저장하고 있는 int 배열
    private Context context;
    private boolean mIsFrontFacing; // 현재 카메라가 전면을 향해 있는지 확인하는 변수

    // 생성자를 통해 필요한 값을 초기화한다.
    FaceGraphic(GraphicOverlay overlay, int maskPosition, Context context, boolean mIsFrontFacing) {
        super(overlay);
        this.maskPosition = maskPosition;
        this.context = context;
        maskDrawable = new int[]{R.drawable.dog, R.drawable.cat, R.drawable.iron, R.drawable.spider, R.drawable.batman, R.drawable.annony, R.drawable.submarine};
        this.mIsFrontFacing = mIsFrontFacing;

    }

    /*
     * 가장 최근 프레임에서 감지한 얼굴의 위치를 업데이트한다.
     * 감지된 위치로 오버레이를 다시 그린다.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /*
     * 얼굴 위치에 Canvas 를 통해 오버레이를 그린다.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }
        // 현재 얼굴의 중심점 (x,y) 를 찾는다.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        // 좌우상하 위치를 찾는다.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;

        if(maskPosition != 0){  // 사용자가 선택한 마스크가 "None" 이 아니라면 마스크를 그린다.
            Drawable d = ContextCompat.getDrawable(context, maskDrawable[maskPosition-1]);  // 선택된 마스크 드로워블을 가져온다.

            // 좌우상하 위치값을 int 형으로 변환한다.
            int l = (int) left;
            int t = (int) top;
            int r = (int) right;
            int b = (int) bottom;

            // 마스크의 위치를 조정할 필요가 있는 경우 조정한다.
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

            // 카메라가 전면을 향해있다면 좌우를 반전시킨다.

            if(mIsFrontFacing){
                Objects.requireNonNull(d).setAutoMirrored(true);
            }else{
                Objects.requireNonNull(d).setAutoMirrored(false);
            }
            Objects.requireNonNull(d).setBounds(l,t,r,b);
            d.draw(canvas);
        }
    }
}
