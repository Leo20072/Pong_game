package com.example.pong_game;

import android.graphics.Color;
import java.util.Random;

public class Ball extends GameObject {
    public float velocityX, velocityY;

    public Ball(float startX, float startY, float size) {
        super(startX, startY, size, size, Color.WHITE);
        reset(startX, startY);
    }

    public void update() {
        x += velocityX;
        y += velocityY;

        rect.left = x;
        rect.top = y;
        rect.right = x + width;
        rect.bottom = y + height;
    }

    public void reset(float newX, float newY) {
        this.x = newX;
        this.y = newY;

        // מהירות התחלתית - בכיוון אופקי בעיקר
        Random r = new Random();
        int directionX = r.nextBoolean() ? 1 : -1;
        int directionY = r.nextBoolean() ? 1 : -1;

        this.velocityX = 20 * directionX; // מהיר יותר בציר ה-X
        this.velocityY = 10 * directionY;
    }
}
