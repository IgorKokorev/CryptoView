package dev.kokorev.cryptoview.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object ViewUtils {
      fun slideView(view: View,
                    currentHeight: Int,
                    newHeight: Int) {
        
        val slideAnimator = ValueAnimator
                .ofInt(currentHeight, newHeight)
            .setDuration(500)
        
        /* We use an update listener which listens to each tick
         * and manually updates the height of the view  */
        
        slideAnimator.addUpdateListener { animation1 ->
                val value = animation1.animatedValue
                view.layoutParams.height = value as Int
                view.requestLayout();
        }
        
        /*  We use an animationSet to play the animation  */
        
        val animationSet = AnimatorSet()
        animationSet.setInterpolator(AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start()
    }
}