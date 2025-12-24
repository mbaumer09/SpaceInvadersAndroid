package com.spaceinvaders.game

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

class Enemy(var x: Float, var y: Float) {
    private val width = 60f
    private val height = 50f
    var isAlive = true
    var moveRight = true

    fun draw(canvas: Canvas, paint: Paint) {
        if (isAlive) {
            // Draw simple enemy shape (rectangle with details)
            canvas.drawRect(x + 10, y, x + width - 10, y + height, paint)
            canvas.drawRect(x, y + 15, x + width, y + height - 15, paint)

            // Draw "eyes"
            paint.color = android.graphics.Color.BLACK
            canvas.drawRect(x + 15, y + 20, x + 25, y + 30, paint)
            canvas.drawRect(x + width - 25, y + 20, x + width - 15, y + 30, paint)
            paint.color = android.graphics.Color.GREEN
        }
    }

    fun getRect(): RectF {
        return RectF(x, y, x + width, y + height)
    }

    fun moveDown() {
        y += 40f
    }
}
