package com.spaceinvaders.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class Bullet(var x: Float, var y: Float, private val speed: Float, val isPlayerBullet: Boolean, private val screenHeight: Int) {
    private val width = 8f
    private val height = 20f
    var isActive = true

    fun update() {
        if (isPlayerBullet) {
            y -= speed
        } else {
            y += speed
        }

        // Deactivate if off screen
        if (y < -height || y > screenHeight) {
            isActive = false
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (isActive) {
            canvas.drawRect(x, y, x + width, y + height, paint)
        }
    }

    fun getRect(): RectF {
        return RectF(x, y, x + width, y + height)
    }
}
