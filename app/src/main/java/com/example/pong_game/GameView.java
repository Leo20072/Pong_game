package com.example.pong_game;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying;
    private boolean isGameOver = false;

    private Ball ball;
    private Paddle playerPaddle;
    private Paddle enemyPaddle;

    private float screenX, screenY;

    private int playerScore = 0;
    private int enemyScore = 0;
    private final int WINNING_SCORE = 5;
    private String winnerMessage = "";

    // משתנה הקובע את מהירות האויב - ככל שזה נמוך יותר, קל יותר לנצח
    private float enemySpeed = 13.0f;

    private Paint textPaint;

    public GameView(Context context, float screenX, float screenY) {
        super(context);
        this.screenX = screenX;
        this.screenY = screenY;

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);

        initGame();
    }

    private void initGame() {
        ball = new Ball(screenX / 2, screenY / 2, 40);

        float paddleWidth = 40;
        float paddleHeight = 200;

        // שחקן בצד ימין
        playerPaddle = new Paddle(screenY, screenX - 100, screenY / 2 - 100, paddleWidth, paddleHeight);

        // מחשב בצד שמאל
        enemyPaddle = new Paddle(screenY, 50, screenY / 2 - 100, paddleWidth, paddleHeight);

        playerScore = 0;
        enemyScore = 0;
        isGameOver = false;
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!isGameOver) {
                update();
            }
            draw();
            control();
        }
    }

    private void update() {
        ball.update();

        // --- קירות (תקרה ורצפה) ---
        if (ball.y <= 0 || ball.y + ball.height >= screenY) {
            ball.velocityY = -ball.velocityY;
        }

        // --- גולים ---
        // יצא בצד שמאל (מחשב פספס) -> נקודה לשחקן
        if (ball.x < 0) {
            playerScore++;
            checkGameOver();
            ball.reset(screenX / 2, screenY / 2);
        }

        // יצא בצד ימין (שחקן פספס) -> נקודה למחשב
        if (ball.x > screenX) {
            enemyScore++;
            checkGameOver();
            ball.reset(screenX / 2, screenY / 2);
        }

        // --- התנגשויות ---
        if (android.graphics.RectF.intersects(playerPaddle.getRect(), ball.getRect())) {
            ball.velocityX = -Math.abs(ball.velocityX); // העפה שמאלה
            ball.x = playerPaddle.x - ball.width - 2;
            increaseBallSpeed();
        }

        if (android.graphics.RectF.intersects(enemyPaddle.getRect(), ball.getRect())) {
            ball.velocityX = Math.abs(ball.velocityX); // העפה ימינה
            ball.x = enemyPaddle.x + enemyPaddle.width + 2;
            increaseBallSpeed();
        }

        // --- אינטליגנציה מלאכותית הוגנת (השינוי המבוקש) ---
        float targetY = ball.y - (enemyPaddle.height / 2);

        // המחשב זז במהירות מוגבלת (enemySpeed) לכיוון הכדור
        if (enemyPaddle.y < targetY) {
            float nextY = enemyPaddle.y + enemySpeed;
            if (nextY > targetY) nextY = targetY; // שלא יעבור את המטרה בטעות
            enemyPaddle.update(nextY);
        }
        else if (enemyPaddle.y > targetY) {
            float nextY = enemyPaddle.y - enemySpeed;
            if (nextY < targetY) nextY = targetY;
            enemyPaddle.update(nextY);
        }
    }

    // פונקציה להאצת המשחק ככל שהוא מתקדם
    private void increaseBallSpeed() {
        ball.velocityX *= 1.05;
        ball.velocityY *= 1.05;
    }

    private void checkGameOver() {
        if (playerScore >= WINNING_SCORE) {
            isGameOver = true;
            winnerMessage = "YOU WIN!";
        } else if (enemyScore >= WINNING_SCORE) {
            isGameOver = true;
            winnerMessage = "GAME OVER";
        }
    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.BLACK);

            // רשת באמצע
            Paint netPaint = new Paint();
            netPaint.setColor(Color.GRAY);
            netPaint.setStrokeWidth(5);
            canvas.drawLine(screenX / 2, 0, screenX / 2, screenY, netPaint);

            ball.draw(canvas);
            playerPaddle.draw(canvas);
            enemyPaddle.draw(canvas);

            // ניקוד
            canvas.drawText(String.valueOf(enemyScore), screenX / 4, 100, textPaint);
            canvas.drawText(String.valueOf(playerScore), screenX * 3 / 4, 100, textPaint);

            if (isGameOver) {
                textPaint.setTextSize(100);
                canvas.drawText(winnerMessage, screenX / 2, screenY / 2, textPaint);
                textPaint.setTextSize(50);
                canvas.drawText("Tap to Restart", screenX / 2, screenY / 2 + 100, textPaint);
                textPaint.setTextSize(60); // החזרת גודל לניקוד
            }

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isGameOver && event.getAction() == MotionEvent.ACTION_DOWN) {
            initGame();
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                playerPaddle.update(event.getY() - (playerPaddle.height / 2));
                break;
        }
        return true;
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }
}