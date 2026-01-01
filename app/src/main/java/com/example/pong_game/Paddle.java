package com.example.pong_game;

import android.graphics.Color;

public class Paddle extends GameObject {
    private float screenHeight; // שומרים את גובה המסך במקום הרוחב

    public Paddle(float screenHeight, float startX, float startY, float w, float h) {
        super(startX, startY, w, h, Color.CYAN);
        this.screenHeight = screenHeight;
    }

    // הפונקציה מקבלת Y חדש במקום X
    public void update(float newY) {
        y = newY;

        // מניעת יציאה מהגבולות (למעלה ולמטה)
        if (y < 0) y = 0;
        if (y + height > screenHeight) y = screenHeight - height;

        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;
    }
}
