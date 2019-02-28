package com.jr.sibi.todo.fragment.Todo;

/**
 * Created by sibi-4939 on 19/04/18.
 */

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationHelper {

    private static final int SECONDS = 300;
    private static final float VISIBLE = 1.0f;
    private static final float INVISIBLE = 0.0f;

    /**
     * @return A fade out animation from 100% - 0% taking half a second
     */
    public static Animation createFadeoutAnimation() {
        Animation fadeout = new AlphaAnimation(VISIBLE, INVISIBLE);
        fadeout.setDuration(SECONDS);
        return fadeout;
    }

    /**
     * @return A fade in animation from 0% - 100% taking half a second
     */
    public static Animation createFadeInAnimation() {
        Animation animation = new AlphaAnimation(INVISIBLE, VISIBLE);
        animation.setDuration(SECONDS);
        return animation;
    }

    public static Animation slideOutRight(Context context){
        Animation animation = AnimationUtils.loadAnimation(context,android.R.anim.slide_out_right);
        animation.setDuration(SECONDS);
        return animation;

    }

}
