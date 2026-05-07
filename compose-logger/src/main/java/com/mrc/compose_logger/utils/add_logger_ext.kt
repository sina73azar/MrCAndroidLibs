package com.mrc.compose_logger.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mrc.compose_logger.R
import com.mrc.compose_logger.presentation.LoggerActivity

@SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
fun AppCompatActivity.addDebugLogger() {
//    if (BuildConfig.DEBUG) {

        val button = FloatingActionButton(this).apply {
            setImageResource(R.drawable.ic_circle_info)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tooltipText = "Network logs"
            }
        }

        addContentView(
            button,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = 32
                bottomMargin = 32
            }
        )

        // Add draggable + clickable
        var dX = 0f
        var dY = 0f
        var startX = 0f
        var startY = 0f
        val clickThreshold = 15f   // pixels


        button.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    startX = event.rawX
                    startY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }

                MotionEvent.ACTION_UP -> {
                    val deltaX = Math.abs(event.rawX - startX)
                    val deltaY = Math.abs(event.rawY - startY)

                    val isClick = deltaX < clickThreshold && deltaY < clickThreshold

                    if (isClick) {
                        // Trigger regular click
                        v.performClick()
                    }
                }
            }
            true
        }

        // This stays intact and works again!
        button.setOnClickListener {
            startActivity(Intent(this@addDebugLogger, LoggerActivity::class.java))
        }
//    }
}


@SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
fun ComponentActivity.addDebugLogger() {
//    if (BuildConfig.DEBUG) {

    val button = FloatingActionButton(this).apply {
        setImageResource(R.drawable.ic_circle_info)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tooltipText = "Network logs"
        }
    }

    addContentView(
        button,
        FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            marginEnd = 32
            bottomMargin = 32
        }
    )

    // Add draggable + clickable
    var dX = 0f
    var dY = 0f
    var startX = 0f
    var startY = 0f
    val clickThreshold = 15f   // pixels


    button.setOnTouchListener { v, event ->
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                dX = v.x - event.rawX
                dY = v.y - event.rawY
                startX = event.rawX
                startY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                v.animate()
                    .x(event.rawX + dX)
                    .y(event.rawY + dY)
                    .setDuration(0)
                    .start()
            }

            MotionEvent.ACTION_UP -> {
                val deltaX = Math.abs(event.rawX - startX)
                val deltaY = Math.abs(event.rawY - startY)

                val isClick = deltaX < clickThreshold && deltaY < clickThreshold

                if (isClick) {
                    // Trigger regular click
                    v.performClick()
                }
            }
        }
        true
    }

    // This stays intact and works again!
    button.setOnClickListener {
        startActivity(Intent(this@addDebugLogger, LoggerActivity::class.java))
    }
//    }
}