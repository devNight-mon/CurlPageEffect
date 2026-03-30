package com.devnight.pagecurleffect.utilities

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator

/**
 * Created by Efe Şen on 26,03, 2026
 */
class MyGLSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer : PageCurlRenderer
    var onPageFlipped: ((isNext: Boolean) -> Unit)? = null
    private var isAnimating = false

    init {
        setEGLContextClientVersion(2)
        renderer = PageCurlRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private var startX = 0f
    private var startY = 0f
    private val SWIPE_THRESHOLD = 0.2f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x / width
        val y = event.y / height

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val corner = if (startX < 0.5f) -1f else 1f
                renderer.setTouch(x * 2 - 1, 1 - y * 2, corner)
                requestRender()
            }
            MotionEvent.ACTION_UP -> {
                val deltaX = x - startX
                if (deltaX < -SWIPE_THRESHOLD) {
                    // sağdan sola swipe → NEXT
                    animateNextPage {}
                } else if (deltaX > SWIPE_THRESHOLD) {
                    // soldan sağa swipe → PREV
                    animatePrevPage {}
                } else {
                    startFlipAnimation(x * 2 - 1, 1 - y * 2, complete = false)
                }
            }
        }
        return true
    }

    fun animateNextPage(onComplete: () -> Unit) {
        if (isAnimating) return
        isAnimating = true

        val animator = ValueAnimator.ofFloat(1.1f, -1.2f)
        animator.duration = 600
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            renderer.setTouch(value, -0.2f, 1f)
            requestRender()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
                renderer.setTouch(1.1f, -1.1f)
                requestRender()
                onComplete()
                onPageFlipped?.invoke(true) // <-- add this line to notify page flipped
            }
            override fun onAnimationCancel(animation: Animator) { isAnimating = false }
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    fun animatePrevPage(onComplete: () -> Unit) {
        if (isAnimating) return
        isAnimating = true

        val animator = ValueAnimator.ofFloat(-1.2f, 1.1f)
        animator.duration = 600
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            renderer.setTouch(value, -0.2f, -1f)
            requestRender()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
                renderer.setTouch(1.1f, -1.1f)
                requestRender()
                onComplete()
                onPageFlipped?.invoke(false)
            }
            override fun onAnimationCancel(animation: Animator) { isAnimating = false }
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    private fun startFlipAnimation(startX: Float, startY: Float, complete: Boolean) {
        isAnimating = true
        val targetX = if (complete) -1.2f else 1.1f
        val animator = ValueAnimator.ofFloat(startX, targetX)
        animator.duration = 400
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val corner = if (complete) 1f else -1f
            renderer.setTouch(value, startY, corner)
            requestRender()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                isAnimating = false
                if (complete) onPageFlipped?.invoke(complete)
                renderer.setTouch(1.1f, -1.1f)
                requestRender()
            }
            override fun onAnimationCancel(animation: Animator) { isAnimating = false }
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    fun setPages(front: Bitmap, next: Bitmap?) {
        renderer.setPages(front, next)
        requestRender()
    }
}