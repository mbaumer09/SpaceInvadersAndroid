package com.spaceinvaders.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class GameView(context: Context) : SurfaceView(context), Runnable {
    private var gameThread: Thread? = null
    private var isPlaying = false
    private val surfaceHolder: SurfaceHolder = holder

    private lateinit var canvas: Canvas
    private val paint = Paint()

    private var screenWidth = 0
    private var screenHeight = 0

    private lateinit var player: Player
    private val enemies = mutableListOf<Enemy>()
    private val playerBullets = CopyOnWriteArrayList<Bullet>()
    private val enemyBullets = CopyOnWriteArrayList<Bullet>()

    private var score = 0
    private var level = 1
    private var gameState = GameState.PLAYING

    private val enemySpeed = 5f
    private var enemyMoveDownCounter = 0
    private var lastShotTime = 0L
    private val shootCooldown = 300L

    private var touchX = 0f
    private var isShooting = false
    private var isInitialized = false

    enum class GameState {
        PLAYING,
        GAME_OVER,
        WIN
    }

    override fun run() {
        while (isPlaying) {
            update()
            draw()
            control()
        }
    }

    private fun update() {
        if (!isInitialized || gameState != GameState.PLAYING) return

        // Update player bullets
        playerBullets.forEach { it.update() }
        playerBullets.removeAll { !it.isActive }

        // Update enemy bullets
        enemyBullets.forEach { it.update() }
        enemyBullets.removeAll { !it.isActive }

        // Move enemies
        updateEnemies()

        // Enemy random shooting
        if (Random.nextInt(100) < 2) {
            val aliveEnemies = enemies.filter { it.isAlive }
            if (aliveEnemies.isNotEmpty()) {
                val shooter = aliveEnemies.random()
                enemyBullets.add(Bullet(shooter.x + 30, shooter.y + 50, 12f, false, screenHeight))
            }
        }

        // Check collisions
        checkCollisions()

        // Check win condition
        if (enemies.none { it.isAlive }) {
            nextLevel()
        }

        // Check lose condition
        if (player.lives <= 0 || enemies.any { it.isAlive && it.y > screenHeight - 200 }) {
            gameState = GameState.GAME_OVER
        }
    }

    private fun updateEnemies() {
        var shouldMoveDown = false
        var newDirection = false

        // Check if any enemy hit the edge
        enemies.filter { it.isAlive }.forEach { enemy ->
            if (enemy.moveRight && enemy.x > screenWidth - 80) {
                shouldMoveDown = true
                newDirection = true
            } else if (!enemy.moveRight && enemy.x < 20) {
                shouldMoveDown = true
                newDirection = true
            }
        }

        if (shouldMoveDown) {
            enemies.forEach { enemy ->
                if (enemy.isAlive) {
                    enemy.moveDown()
                    enemy.moveRight = !enemy.moveRight
                }
            }
        } else {
            enemies.forEach { enemy ->
                if (enemy.isAlive) {
                    if (enemy.moveRight) {
                        enemy.x += enemySpeed
                    } else {
                        enemy.x -= enemySpeed
                    }
                }
            }
        }
    }

    private fun checkCollisions() {
        // Player bullets vs enemies
        playerBullets.forEach { bullet ->
            enemies.forEach { enemy ->
                if (bullet.isActive && enemy.isAlive &&
                    RectF.intersects(bullet.getRect(), enemy.getRect())
                ) {
                    bullet.isActive = false
                    enemy.isAlive = false
                    score += 10
                }
            }
        }

        // Enemy bullets vs player
        enemyBullets.forEach { bullet ->
            if (bullet.isActive && RectF.intersects(bullet.getRect(), player.getRect())) {
                bullet.isActive = false
                player.lives--
            }
        }
    }

    private fun nextLevel() {
        level++
        score += 100
        createEnemies()
        playerBullets.clear()
        enemyBullets.clear()
    }

    private fun draw() {
        if (!isInitialized || !surfaceHolder.surface.isValid) return
        canvas = surfaceHolder.lockCanvas() ?: return

        // Clear screen
        canvas.drawColor(Color.BLACK)

        paint.color = Color.GREEN

        when (gameState) {
            GameState.PLAYING -> drawGame()
            GameState.GAME_OVER -> drawGameOver()
            GameState.WIN -> drawWin()
        }

        surfaceHolder.unlockCanvasAndPost(canvas)
    }

    private fun drawGame() {
        // Draw player
        player.draw(canvas, paint)

        // Draw enemies
        paint.color = Color.GREEN
        enemies.forEach { it.draw(canvas, paint) }

        // Draw bullets
        paint.color = Color.CYAN
        playerBullets.forEach { it.draw(canvas, paint) }

        paint.color = Color.RED
        enemyBullets.forEach { it.draw(canvas, paint) }

        // Draw UI
        paint.color = Color.WHITE
        paint.textSize = 40f
        canvas.drawText("Score: $score", 30f, 60f, paint)
        canvas.drawText("Lives: ${player.lives}", 30f, 110f, paint)
        canvas.drawText("Level: $level", 30f, 160f, paint)
        canvas.drawText("Bullets: ${playerBullets.size}", 30f, 210f, paint)
        canvas.drawText("Enemy bullets: ${enemyBullets.size}", 30f, 260f, paint)
    }

    private fun drawGameOver() {
        paint.color = Color.RED
        paint.textSize = 80f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f - 100, paint)

        paint.textSize = 50f
        canvas.drawText("Score: $score", screenWidth / 2f, screenHeight / 2f, paint)
        canvas.drawText("Tap to Restart", screenWidth / 2f, screenHeight / 2f + 100, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawWin() {
        paint.color = Color.GREEN
        paint.textSize = 80f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("YOU WIN!", screenWidth / 2f, screenHeight / 2f - 100, paint)

        paint.textSize = 50f
        canvas.drawText("Score: $score", screenWidth / 2f, screenHeight / 2f, paint)
        canvas.drawText("Tap to Continue", screenWidth / 2f, screenHeight / 2f + 100, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun control() {
        try {
            Thread.sleep(16) // ~60 FPS
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun pause() {
        isPlaying = false
        try {
            gameThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        isPlaying = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w
        screenHeight = h

        // Initialize game
        player = Player(screenWidth / 2f - 40f, screenHeight - 150f, screenWidth)
        createEnemies()
        isInitialized = true
    }

    private fun createEnemies() {
        enemies.clear()
        val rows = 4 + (level - 1)
        val cols = 6

        for (row in 0 until rows.coerceAtMost(8)) {
            for (col in 0 until cols) {
                val x = col * 100f + 50f
                val y = row * 80f + 100f
                enemies.add(Enemy(x, y))
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInitialized) return true
        touchX = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (gameState == GameState.PLAYING) {
                    // Move player toward touch position
                    if (touchX < player.x + 40) {
                        player.moveLeft()
                    } else if (touchX > player.x + 40) {
                        player.moveRight()
                    }

                    // Shoot
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastShotTime > shootCooldown) {
                        playerBullets.add(Bullet(player.getCenterX() - 4, player.y, 20f, true, screenHeight))
                        lastShotTime = currentTime
                    }
                } else {
                    // Restart game
                    restartGame()
                }
            }
        }
        return true
    }

    private fun restartGame() {
        score = 0
        level = 1
        player.lives = 3
        playerBullets.clear()
        enemyBullets.clear()
        createEnemies()
        gameState = GameState.PLAYING
    }
}
