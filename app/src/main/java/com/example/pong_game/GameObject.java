package com.example.pong_game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class GameObject {
    protected float x, y;
    protected float width, height;
    protected RectF rect;
    protected Paint paint;

    public GameObject(float x, float y, float width, float height, int color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.rect = new RectF(x, y, x + width, y + height);
        this.paint = new Paint();
        this.paint.setColor(color);
    }

    // פונקציית הציור משותפת לכולם - אין סיבה לכתוב אותה פעמיים
    public void draw(Canvas canvas) {
        canvas.drawRect(rect, paint);
    }

    public RectF getRect() {
        return rect;
    }
}
