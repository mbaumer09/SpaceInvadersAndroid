package com.spaceinvaders.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class Player(var x: Float, var y: Float, private val screenWidth: Int) {
    private val width = 80f
    private val height = 60f
    val speed = 15f
    var lives = 3

    fun moveLeft() {
        x -= speed
        if (x < 0) x = 0f
    }

    fun moveRight() {
        x += speed
        if (x > screenWidth - width) x = screenWidth - width
    }

    fun draw(canvas: Canvas, paint: Paint) {
        // Draw player ship
        // Main body
        canvas.drawRect(x + 25, y + 20, x + 55, y + height, paint)
        // Top cockpit
        canvas.drawRect(x + 30, y, x + 50, y + 30, paint)
        // Wings
        canvas.drawRect(x, y + 40, x + width, y + height, paint)

        // Draw cannon
        paint.color = android.graphics.Color.CYAN
        canvas.drawRect(x + 37, y + 10, x + 43, y + 25, paint)
        paint.color = android.graphics.Color.GREEN
    }

    fun getRect(): RectF {
        return RectF(x, y, x + width, y + height)
    }

    fun getCenterX(): Float {
        return x + width / 2
    }
}
