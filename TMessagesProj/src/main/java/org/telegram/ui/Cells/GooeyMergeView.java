package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;

import java.util.Arrays;

public class GooeyMergeView extends FrameLayout {
    private static final float DEFAULT_TOOLBAR_HEIGHT = 32;

    private final Paint fadeToTop = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fadeToBottom = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float gooeynessFactor;
    private boolean enabled;

    private final Paint filter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Bitmap[] bitmaps = new Bitmap[2];
    private final Canvas[] bitmapCanvas = new Canvas[bitmaps.length];
    private final Paint bitmapPaint = new Paint(Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);

    private AndroidUtilities.CutoutInfo cutoutInfo;

    public GooeyMergeView(Context context) {
        super(context);

        blackPaint.setColor(Color.BLACK);

        setGooeynessFactor(10f);
        setWillNotDraw(false);
    }

    public void setGooeynessFactor(float gooeynessFactor) {
        this.gooeynessFactor = gooeynessFactor;
        filter.setColorFilter(new ColorMatrixColorFilter(new float[]{
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 4f * gooeynessFactor, -125 * 4 * gooeynessFactor
        }));
        invalidate();
    }

    public void setGooeyEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cutoutInfo = AndroidUtilities.cutoutInfo;
        if (cutoutInfo != null && (cutoutInfo.gravity != Gravity.CENTER) || getWidth() > getHeight()) {
            cutoutInfo = null;
        }

        for (Bitmap bm : bitmaps) {
            if (bm != null) {
                bm.recycle();
            }
        }
        bitmaps[0] = Bitmap.createBitmap(w, h + AndroidUtilities.dp(DEFAULT_TOOLBAR_HEIGHT), Bitmap.Config.ARGB_8888);
        bitmapCanvas[0] = new Canvas(bitmaps[0]);

        bitmaps[1] = Bitmap.createBitmap(w / 4, h / 4, Bitmap.Config.ARGB_8888);
        bitmapCanvas[1] = new Canvas(bitmaps[1]);

        fadeToTop.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        fadeToTop.setShader(new LinearGradient(getWidth() / 2f, 0, getWidth() / 2f, AndroidUtilities.dp(100), new int[]{0xFF000000, 0xFFFFFFFF}, new float[]{0.15f, 1f}, Shader.TileMode.CLAMP));

        fadeToBottom.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        fadeToBottom.setShader(new LinearGradient(getWidth() / 2f, 0, getWidth() / 2f, AndroidUtilities.dp(100), new int[]{0xFFFFFFFF, 0xFF000000}, new float[]{0.25f, 1f}, Shader.TileMode.CLAMP));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!enabled) {
            super.draw(canvas);
            return;
        }

        if (bitmaps[0] == null) return;

        for (int i = 0; i < 2; i++) {
            bitmaps[i].eraseColor(0);
        }

        bitmapCanvas[0].save();
        bitmapCanvas[0].translate(0, AndroidUtilities.dp(DEFAULT_TOOLBAR_HEIGHT));
        super.draw(bitmapCanvas[0]);
        bitmapCanvas[0].restore();

        float scX = bitmaps[1].getWidth() / (float) bitmaps[0].getWidth(),
                scY = bitmaps[1].getWidth() / (float) bitmaps[0].getWidth();
        bitmapCanvas[1].save();
        bitmapCanvas[1].scale(scX, scY);
        bitmapCanvas[1].drawBitmap(bitmaps[0], 0, 0, null);
        if (cutoutInfo != null) {
            bitmapCanvas[1].translate(0, AndroidUtilities.dp(DEFAULT_TOOLBAR_HEIGHT));
            if (cutoutInfo.isProbablyCircle) {
                float rad = Math.min(cutoutInfo.bounds.width(), cutoutInfo.bounds.height()) / 2f;
                bitmapCanvas[1].drawCircle(cutoutInfo.bounds.centerX(), cutoutInfo.bounds.bottom - cutoutInfo.bounds.width() / 2f, rad, blackPaint);
            } else if (cutoutInfo.isAccurate) {
                bitmapCanvas[1].drawPath(cutoutInfo.path, blackPaint);
            } else {
                float rad = Math.max(cutoutInfo.bounds.width(), cutoutInfo.bounds.height()) / 2f;
                bitmapCanvas[1].drawRoundRect(cutoutInfo.bounds, rad, rad, blackPaint);
            }
        } else {
            bitmapCanvas[1].drawRect(0, 0, getWidth(), AndroidUtilities.dp(DEFAULT_TOOLBAR_HEIGHT), blackPaint);
        }
        bitmapCanvas[1].restore();

        Utilities.stackBlurBitmap(bitmaps[1], (int) (gooeynessFactor / 2));

        canvas.translate(0, -AndroidUtilities.dp(DEFAULT_TOOLBAR_HEIGHT));
        canvas.save();

        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.saveLayer(0, 0, getWidth(), getHeight(), filter);
        canvas.scale(1f / scX, 1f / scY);
        canvas.drawBitmap(bitmaps[1], 0, 0, bitmapPaint);
        canvas.restore();
        canvas.drawRect(0, 0, getWidth(), getHeight(), fadeToBottom);
        canvas.restore();

        canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        bitmapPaint.setAlpha(0xFF);
        canvas.drawBitmap(bitmaps[0], 0, 0, bitmapPaint);
        canvas.drawRect(0, 0, getWidth(), getHeight(), fadeToTop);
        canvas.restore();

        canvas.restore();

        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (Bitmap bm : bitmaps) {
            if (bm != null) {
                bm.recycle();
            }
        }
        Arrays.fill(bitmaps, null);
        Arrays.fill(bitmapCanvas, null);
    }
}