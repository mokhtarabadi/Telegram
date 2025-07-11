package org.telegram.ui.Cells;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;

import java.util.ArrayList;
import java.util.List;

public class ProfileHeaderActionsView extends View {

    public interface OnActionClickListener {
        void onClick(View anchor, float x, float y);
    }

    public static class ActionButton {
        private final int iconResId;
        private final String text;
        private final OnActionClickListener action;
        private final boolean isChecked;

        public ActionButton(int iconResId, String text, @Nullable String contentDescription, boolean isChecked, OnActionClickListener action) {
            this.iconResId = iconResId;
            this.text = text;
            this.action = action;
            this.isChecked = isChecked;
        }

        public ActionButton(int iconResId, String text, @Nullable String contentDescription, boolean isChecked, Runnable action) {
            this(iconResId, text, contentDescription, isChecked, (anchor, x, y) -> {
                if (action != null) {
                    action.run();
                }
            });
        }

        public ActionButton(int iconResId, String text, boolean isChecked, Runnable action) {
            this(iconResId, text, null, isChecked, (anchor, x, y) -> {
                if (action != null) {
                    action.run();
                }
            });
        }

        public String getText() {
            return text;
        }

        public boolean isChecked() {
            return isChecked;
        }
    }

    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<ActionButton> currentActions = new ArrayList<>();
    private final List<Drawable> currentIcons = new ArrayList<>();
    private final List<RectF> buttonRects = new ArrayList<>();

    private final Interpolator interpolator = CubicBezierInterpolator.DEFAULT;

    private int pressedButtonIndex = -1;
    private float rippleX, rippleY, rippleProgress;
    private ValueAnimator rippleAnimator;

    private final int buttonHeight = AndroidUtilities.dp(56);
    private final int buttonSpacing = AndroidUtilities.dp(6);
    private final int horizontalPadding = AndroidUtilities.dp(12);
    private final int cornerRadius = AndroidUtilities.dp(10);

    private final Theme.ResourcesProvider resourcesProvider;

    public ProfileHeaderActionsView(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        textPaint.setTextSize(AndroidUtilities.dp(12));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        rebuildDrawables();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelRippleAnimation();
    }

    public void setActions(List<ActionButton> actions) {
        if (actions.size() > 4) {
            actions = actions.subList(0, 4);
        }

        currentActions.clear();
        currentActions.addAll(actions);

        rebuildDrawables();
        requestLayout();
        invalidate();
    }

    private void rebuildDrawables() {
        currentIcons.clear();
        buttonRects.clear();

        int iconAndTextColor = getThemedColor(Theme.key_actionBarDefaultIcon);
        textPaint.setColor(iconAndTextColor);

        for (int i = 0; i < currentActions.size(); i++) {
            ActionButton button = currentActions.get(i);
            Drawable icon = ContextCompat.getDrawable(getContext(), button.iconResId).mutate();
            icon.setColorFilter(new PorterDuffColorFilter(iconAndTextColor, PorterDuff.Mode.SRC_IN));
            currentIcons.add(icon);
            buttonRects.add(new RectF());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = buttonHeight;
        setMeasuredDimension(width, height);

        if (currentActions.isEmpty()) {
            return;
        }

        int count = currentActions.size();
        int availableWidth = width - 2 * horizontalPadding;
        int totalSpacing = (count > 1) ? (count - 1) * buttonSpacing : 0;
        int singleButtonWidth = (availableWidth - totalSpacing) / count;

        float currentX = horizontalPadding;
        float buttonTop = 0;
        float buttonBottom = buttonTop + buttonHeight;

        for (int i = 0; i < count; i++) {
            RectF rect = buttonRects.get(i);
            rect.set(
                    currentX,
                    buttonTop,
                    currentX + singleButtonWidth,
                    buttonBottom
            );
            currentX += singleButtonWidth + buttonSpacing;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //if (Theme.isCurrentThemeDark()) {
            //backgroundPaint.setColor(Color.argb(20, 255, 255, 255));
        //} else {
            backgroundPaint.setColor(Color.argb(40, 0, 0, 0));
        //}

        for (RectF rect : buttonRects) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint);
        }

        for (int i = 0; i < currentActions.size(); i++) {
            RectF rect = buttonRects.get(i);

            if (pressedButtonIndex == i && rippleAnimator != null) {
                ripplePaint.setColor(ColorUtils.setAlphaComponent(getThemedColor(Theme.key_actionBarDefaultIcon), 30));
                canvas.drawCircle(rippleX, rippleY, (rect.width() / 2.0f) * rippleProgress, ripplePaint);
            }

            Drawable icon = currentIcons.get(i);
            String text = currentActions.get(i).text;

            int iconSize = AndroidUtilities.dp(26);
            float textHeight = textPaint.descent() - textPaint.ascent();
            int iconTextSpacing = AndroidUtilities.dp(4);

            float totalContentHeight = iconSize + iconTextSpacing + textHeight;
            float contentTop = rect.top + (rect.height() - totalContentHeight) / 2.0f;

            int iconTop = (int) contentTop;
            icon.setBounds(
                    (int) rect.centerX() - iconSize / 2,
                    iconTop,
                    (int) rect.centerX() + iconSize / 2,
                    iconTop + iconSize
            );
            icon.draw(canvas);

            float textY = iconTop + iconSize + iconTextSpacing - textPaint.ascent();
            canvas.drawText(text, rect.centerX(), textY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (currentActions.isEmpty()) {
            return false;
        }

        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < buttonRects.size(); i++) {
                if (buttonRects.get(i).contains(x, y)) {
                    pressedButtonIndex = i;
                    rippleX = x;
                    rippleY = y;
                    startRippleAnimation();
                    invalidate();
                    return true;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (pressedButtonIndex != -1) {
                if (!buttonRects.get(pressedButtonIndex).contains(x, y)) {
                    cancelRippleAnimation();
                    pressedButtonIndex = -1;
                    invalidate();
                }
            }
        } else if (action == MotionEvent.ACTION_UP) {
            if (pressedButtonIndex != -1) {
                if (buttonRects.get(pressedButtonIndex).contains(x, y)) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    currentActions.get(pressedButtonIndex).action.onClick(this, x, y);
                }
                cancelRippleAnimation();
                pressedButtonIndex = -1;
                invalidate();
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            if (pressedButtonIndex != -1) {
                cancelRippleAnimation();
                pressedButtonIndex = -1;
                invalidate();
            }
        }

        return pressedButtonIndex != -1;
    }

    private void startRippleAnimation() {
        cancelRippleAnimation();
        rippleAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        rippleAnimator.setDuration(250);
        rippleAnimator.setInterpolator(interpolator);
        rippleAnimator.addUpdateListener(animation -> {
            rippleProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        rippleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rippleProgress = 0.0f;
                if (pressedButtonIndex == -1) {
                    invalidate();
                }
            }
        });
        rippleAnimator.start();
    }

    private void cancelRippleAnimation() {
        if (rippleAnimator != null) {
            rippleAnimator.cancel();
            rippleAnimator = null;
        }
        rippleProgress = 0.0f;
    }

    private int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }
}