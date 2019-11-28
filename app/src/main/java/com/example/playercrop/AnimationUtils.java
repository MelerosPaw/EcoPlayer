package com.example.playercrop;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import androidx.annotation.NonNull;

/** Class containing animations to show/hide views and startUrl activities. */
public class AnimationUtils {

  public static void animateViewSetWithAlpha(View view1, int alphaView1, View view2,
      int alphaView2, Integer animationDefaultTime) {
    if (view1 != null && view2 != null && animationDefaultTime != null) {
      AnimatorSet animatorSet = new AnimatorSet();
      animatorSet.playTogether(
          ObjectAnimator.ofFloat(view1, "alpha", alphaView1),
          ObjectAnimator.ofFloat(view2, "alpha", alphaView2));
      animatorSet.setDuration(animationDefaultTime).start();
    }
  }

  /**
   * Sets an animation to fade out a view in 500 milliseconds. Calls {@link #fadeOut(long)}.
   * You must set the view's visibility to {@link android.view.View#GONE} or
   * {@link android.view.View#INVISIBLE} after the animation ends.
   *
   * @return An {@code Animation} set to fade out a view.
   */
  public static Animation fadeOut() {
    return fadeOut(500);
  }

  /**
   * Sets an animation to fade out a view in {@code duration} milliseconds. You must set the
   * view's visibility to {@link android.view.View#GONE} or {@link android.view.View#INVISIBLE}
   * after the animation ends.
   *
   * @param duration The duration of the animation in milliseconds.
   * @return An {@code Animation} set to fade out a view.
   */
  public static Animation fadeOut(long duration) {
    Animation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setInterpolator(new DecelerateInterpolator());
    fadeOut.setDuration(duration);
    return fadeOut;
  }


  /**
   * A custom listener that implements {@code AnimationListener} and overrides
   * {@code onAnimationStart()} and {@code onAnimationRepeat()} leaving their bodies empty. It can
   * be used in place of {@code AnimationListener} if you only need to override
   * {@code onAnimationEnd()}, avoiding thus having to add pointless boilerplate code to your
   * class.
   * <br/><br/>
   * Whenever you need to do something after the animation is over but not when it starts nor
   * while the animation is in progress, you can add an {@code AnimationEndListener} to your
   * {@code Animation} like this:
   *
   * <pre>
   * animation.setAnimationListener(new AnimationEndListener() {
   *      {@literal@}Override
   *      public void onAnimationEnd(Animation animation) {
   *          //Code to be executed when animation ends.
   *      }
   * });</pre>
   */
  public static abstract class AnimationEndListener implements Animation.AnimationListener {

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
  }
}