package com.example.player.Modal;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;

public class AnimationUtil {

    public static int ANIMATION_DURATION_SHORT = 150;
    public static int ANIMATION_DURATION_MEDIUM = 400;
    public static int ANIMATION_DURATION_LONG = 800;

    public static void crossFadeViews(View showView, View hideView) {
        crossFadeViews(showView, hideView, ANIMATION_DURATION_SHORT);
    }

    public static void crossFadeViews(View showView, final View hideView, int duration) {
        fadeInView(showView, duration);
        fadeOutView(hideView, duration);
    }

    public static void fadeInView(View view, int duration) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);

        ViewCompat.animate(view).alpha(1f).setDuration(duration);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void reveal(final View view) {
        int cx = view.getWidth() - (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, view.getResources().getDisplayMetrics());
        int cy = view.getHeight() / 2;
        int finalRadius = Math.max(view.getWidth(), view.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        view.setVisibility(View.VISIBLE);

        anim.start();
    }

    public static void fadeOutView(View view) {
        fadeOutView(view, ANIMATION_DURATION_SHORT);
    }


    public static void fadeOutView(View view, int duration) {
        ViewCompat.animate(view).alpha(0f).setDuration(duration);
    }
}