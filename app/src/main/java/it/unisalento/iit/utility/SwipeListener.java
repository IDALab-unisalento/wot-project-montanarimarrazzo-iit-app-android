package it.unisalento.iit.utility;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import it.unisalento.iit.R;

public class SwipeListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public SwipeListener (Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 1;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            versoDestra();
                        } else {
                            versoSinistra();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void versoDestra() {
    }

    public void versoSinistra() {
    }

    //Activity corrente
    public static void swipe(String verso, Activity activity) {
        switch(verso.toLowerCase()){
            case("avanti"):
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); //passa all'altra activity effetto Swipe verso Sinistra
                break;
            case("indietro"):
                activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); //passa all'altra activity effetto Swipe verso Sinistra
                break;
        }
    }

}