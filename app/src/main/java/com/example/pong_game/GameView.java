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
        // כדור באמצע
        ball = new Ball(screenX / 2, screenY / 2, 40);

        // הגדרת גודל מחבטים: צרים וגבוהים (בניגוד לקודם שהיו רחבים ונמוכים)
        float paddleWidth = 40;
        float paddleHeight = 200;

        // שחקן בצד ימין (קרוב לאגודל ימין)
        playerPaddle = new Paddle(screenY, screenX - 100, screenY / 2 - 100, paddleWidth, paddleHeight);

        // אויב בצד שמאל
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

        // --- קירות (למעלה ולמטה) ---
        // אם הכדור פוגע בתקרה או ברצפה - הוא חוזר
        if (ball.y <= 0 || ball.y + ball.height >= screenY) {
            ball.velocityY = -ball.velocityY;
        }

        // --- גולים (ימין ושמאל) ---

        // יצא בצד שמאל (מאחורי האויב) -> נקודה לשחקן
        if (ball.x < 0) {
            playerScore++;
            checkGameOver();
            ball.reset(screenX / 2, screenY / 2);
        }

        // יצא בצד ימין (מאחורי השחקן) -> נקודה לאויב
        if (ball.x > screenX) {
            enemyScore++;
            checkGameOver();
            ball.reset(screenX / 2, screenY / 2);
        }

        // --- התנגשות במחבטים ---

        // בדיקה מול השחקן
        if (android.graphics.RectF.intersects(playerPaddle.getRect(), ball.getRect())) {
            ball.velocityX = -Math.abs(ball.velocityX); // מעיף שמאלה
            ball.x = playerPaddle.x - ball.width - 2; // למנוע היתקעות

            // הגברת מהירות קלה לאתגר
            ball.velocityX *= 1.05;
        }

        // בדיקה מול האויב
        if (android.graphics.RectF.intersects(enemyPaddle.getRect(), ball.getRect())) {
            ball.velocityX = Math.abs(ball.velocityX); // מעיף ימינה
            ball.x = enemyPaddle.x + enemyPaddle.width + 2;
        }

        // --- בינה מלאכותית (AI) ---
        // האויב מנסה להתאים את ה-Y שלו ל-Y של הכדור
        enemyPaddle.update(ball.y - (enemyPaddle.height / 2));
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

            // קו הפרדה באמצע (רשת)
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
                textPaint.setTextSize(60); // החזרת גודל
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

        // השליטה לפי גובה האצבע (Y)
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
