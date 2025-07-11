package org.telegram;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewDebugUtils {
    public static void logViewInfo(View view, String viewName) {
        if (view == null) {
            Log.d("ViewDebug", viewName + " is null");
            return;
        }

        float x = view.getX();
        float y = view.getY();
        float tX = view.getTranslationX();
        float tY = view.getTranslationY();
        float pX = view.getPivotX();
        float pY = view.getPivotY();
        float sX = view.getScaleX();
        float sY = view.getScaleY();
        float r = view.getRotation();
        float rX = view.getRotationX();
        float rY = view.getRotationY();
        int w = view.getWidth();
        int h = view.getHeight();
        float a = view.getAlpha();
        String vis = visibilityToString(view.getVisibility());

        int mW = view.getMeasuredWidth();
        int mH = view.getMeasuredHeight();
        int mS = view.getMeasuredState();

        int padL = view.getPaddingLeft();
        int padT = view.getPaddingTop();
        int padR = view.getPaddingRight();
        int padB = view.getPaddingBottom();

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        String lpDesc = "";
        if (lp != null) {
            int lpW = lp.width;
            int lpH = lp.height;
            lpDesc = String.format("lp(width=%s, height=%s)", sizeToString(lpW), sizeToString(lpH));

            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                lpDesc += String.format(
                        ", margins=(%d,%d,%d,%d)",
                        mlp.leftMargin, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin
                );
            }
            if (lp instanceof FrameLayout.LayoutParams) {
                int g = ((FrameLayout.LayoutParams) lp).gravity;
                lpDesc += ", gravity=" + gravityToString(g);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(viewName).append(":\n")
                .append(String.format("  Position:       x=%.1f, y=%.1f\n", x, y))
                .append(String.format("  Translation:    x=%.1f, y=%.1f\n", tX, tY))
                .append(String.format("  Pivot:          x=%.1f, y=%.1f\n", pX, pY))
                .append(String.format("  Scale:          x=%.2f, y=%.2f\n", sX, sY))
                .append(String.format("  Rotation:       r=%.1f, rX=%.1f, rY=%.1f\n", r, rX, rY))
                .append(String.format("  Layout size:    width=%d, height=%d\n", w, h))
                .append(String.format("  Measured size:  width=%d, height=%d, state=0x%08X\n", mW, mH, mS))
                .append(String.format("  Padding:        L=%d, T=%d, R=%d, B=%d\n", padL, padT, padR, padB))
                .append("  ").append(lpDesc).append("\n")
                .append(String.format("  Alpha:          %.2f\n", a))
                .append(String.format("  Visibility:     %s", vis));

        float left = view.getX() - view.getPivotX() * (view.getScaleX() - 1);
        float right = left + view.getWidth() * view.getScaleX();
        float center = (left + right) / 2;
        Log.d("ViewDebug",
                viewName + "  SCALED-LEFT=" + left
                        + "  SCALED-RIGHT=" + right
                        + "  SCALED-CENTER=" + center
                        + "  PARENT-CENTER=" + (((View) view.getParent()).getWidth() / 2f));

        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            String text = tv.getText() != null ? tv.getText().toString() : "<null>";
            float textW = tv.getPaint().measureText(text);
            sb.append("\n  TextView text=\"")
                    .append(text.replace("\n", "\\n"))
                    .append("\"  paintWidth=")
                    .append(String.format("%.1f", textW));
        }

        Log.d("ViewDebug", sb.toString());
    }

    private static String sizeToString(int s) {
        switch (s) {
            case ViewGroup.LayoutParams.MATCH_PARENT:
                return "MATCH_PARENT";
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                return "WRAP_CONTENT";
            default:
                return String.valueOf(s);
        }
    }

    private static String visibilityToString(int visibility) {
        switch (visibility) {
            case View.VISIBLE:
                return "VISIBLE";
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.GONE:
                return "GONE";
            default:
                return "UNKNOWN";
        }
    }

    private static String gravityToString(int gravity) {
        if ((gravity & Gravity.CENTER_HORIZONTAL) != 0) return "CENTER_HORIZONTAL";
        if ((gravity & Gravity.LEFT) != 0) return "LEFT";
        if ((gravity & Gravity.RIGHT) != 0) return "RIGHT";
        if ((gravity & Gravity.CENTER_VERTICAL) != 0) return "CENTER_VERTICAL";
        if ((gravity & Gravity.TOP) != 0) return "TOP";
        if ((gravity & Gravity.BOTTOM) != 0) return "BOTTOM";
        return Integer.toHexString(gravity);
    }

    public static DebugView enable(ViewGroup container) {
        Context context = container.getContext();
        float density = context.getResources().getDisplayMetrics().density;

        DebugView debugView = new DebugView(context);

        int expandedWidth = (int) (300 * density);
        int expandedHeight = (int) (450 * density);
        debugView.setExpandedSize(expandedWidth, expandedHeight);

        int collapsedSize = (int) (DebugView.COLLAPSED_SIZE_DP * density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(collapsedSize, collapsedSize);
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        int margin = (int) (16 * density);
        params.setMargins(margin, margin, margin, margin);

        container.addView(debugView, params);
        return debugView;
    }

    private static class SettingsIconDrawable extends Drawable {
        private final Paint paint;
        private final float density;

        public SettingsIconDrawable(float density) {
            this.density = density;
            this.paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public void draw(Canvas canvas) {
            paint.setStrokeWidth(2f * density);

            int width = getBounds().width();
            int height = getBounds().height();
            float cx = width / 2f;
            float cy = height / 2f;

            float outerRadius = Math.min(width, height) / 2f * 0.8f;
            float innerRadius = outerRadius * 0.5f;
            int numTeeth = 8;
            float toothLength = 4f * density;

            for (int i = 0; i < numTeeth; i++) {
                double angle = Math.toRadians(360.0 * i / numTeeth);
                float startX = cx + (float) (Math.cos(angle) * outerRadius);
                float startY = cy + (float) (Math.sin(angle) * outerRadius);
                float endX = cx + (float) (Math.cos(angle) * (outerRadius + toothLength));
                float endY = cy + (float) (Math.sin(angle) * (outerRadius + toothLength));
                canvas.drawLine(startX, startY, endX, endY, paint);
            }
            canvas.drawCircle(cx, cy, innerRadius, paint);
            canvas.drawCircle(cx, cy, outerRadius, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    public static class DebugView extends FrameLayout {

        public interface OnValueChangeListener {
            void onValueChanged(String key, float value);
        }

        public static final int COLLAPSED_SIZE_DP = 56;
        private static final long ANIMATION_DURATION = 300;

        private LinearLayout container;
        private OnValueChangeListener listener;
        private HashMap<String, Float> variableValues;
        private ScrollView scrollView;
        private ImageView settingsButton;
        private GradientDrawable backgroundDrawable;

        private float dX;
        private float dY;
        private float initialRawX;
        private float initialRawY;
        private boolean isDragging;
        private int touchSlop;

        private boolean isExpanded = false;
        private boolean isAnimating = false;
        private int collapsedSize;
        private int expandedWidth;
        private int expandedHeight;

        public DebugView(Context context) {
            super(context);
            init(context);
        }

        private void init(Context context) {
            float density = getResources().getDisplayMetrics().density;
            touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            collapsedSize = (int) (COLLAPSED_SIZE_DP * density);
            int padding = (int) (16 * density);

            backgroundDrawable = new GradientDrawable();
            backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
            backgroundDrawable.setColor(0xCC212121);
            backgroundDrawable.setCornerRadius(collapsedSize / 2f);
            setBackground(backgroundDrawable);

            setPadding(padding, padding, padding, padding);

            scrollView = new ScrollView(context);
            scrollView.setVisibility(View.GONE);
            scrollView.setAlpha(0f);
            addView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(container, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            settingsButton = new ImageView(getContext());
            settingsButton.setImageDrawable(new SettingsIconDrawable(getResources().getDisplayMetrics().density));
            LayoutParams iconParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            settingsButton.setLayoutParams(iconParams);
            settingsButton.setAlpha(1f);
            addView(settingsButton);

            variableValues = new HashMap<>();
        }

        public void setExpandedSize(int width, int height) {
            this.expandedWidth = width;
            this.expandedHeight = height;
        }

        public void setOnValueChangeListener(OnValueChangeListener listener) {
            this.listener = listener;
        }

        public void addTitle(String text) {
            TextView titleView = new TextView(getContext());
            titleView.setText(text);
            titleView.setTextColor(Color.WHITE);
            titleView.setTextSize(18f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            int topMargin = (int) (8 * getResources().getDisplayMetrics().density);
            int bottomMargin = (int) (4 * getResources().getDisplayMetrics().density);
            params.setMargins(0, topMargin, 0, bottomMargin);

            titleView.setLayoutParams(params);
            container.addView(titleView);
        }

        public void addTextView(String text) {
            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextColor(Color.LTGRAY);
            textView.setTextSize(14f);
            container.addView(textView);
        }

        public void addSeekBar(final String key, String label, final float min, final float max, float initialValue) {
            variableValues.put(key, initialValue);

            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);
            int bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            itemParams.setMargins(0, 0, 0, bottomMargin);
            itemLayout.setLayoutParams(itemParams);

            TextView labelView = new TextView(getContext());
            labelView.setText(label);
            labelView.setTextColor(Color.WHITE);
            labelView.setMinWidth((int) (80 * getResources().getDisplayMetrics().density));
            itemLayout.addView(labelView);

            final SeekBar seekBar = new SeekBar(getContext());
            LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT, 1.0f);
            int margin = (int) (8 * getResources().getDisplayMetrics().density);
            seekBarParams.setMargins(margin, 0, margin, 0);
            seekBar.setLayoutParams(seekBarParams);
            seekBar.setMax(1000);

            final TextView valueView = new TextView(getContext());
            valueView.setTextColor(Color.YELLOW);
            valueView.setMinWidth((int) (50 * getResources().getDisplayMetrics().density));
            valueView.setGravity(Gravity.END);

            float initialProgress = ((initialValue - min) / (max - min)) * 1000f;
            seekBar.setProgress((int) initialProgress);
            valueView.setText(String.format("%.2f", initialValue));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float currentValue = min + (progress / 1000f) * (max - min);
                    valueView.setText(String.format("%.2f", currentValue));
                    variableValues.put(key, currentValue);
                    if (fromUser && listener != null) {
                        listener.onValueChanged(key, currentValue);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            valueView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditValueDialog(key, min, max, valueView, seekBar);
                }
            });

            itemLayout.addView(seekBar);
            itemLayout.addView(valueView);
            container.addView(itemLayout);
        }

        private void showEditValueDialog(final String key, final float min, final float max, final TextView valueView, final SeekBar seekBar) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK);
            builder.setTitle("Set value for \"" + key + "\"");

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            input.setText(String.format(Locale.US, "%.2f", variableValues.get(key)));

            FrameLayout container = new FrameLayout(getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int) (20 * getResources().getDisplayMetrics().density);
            params.setMargins(margin, margin, margin, margin);
            input.setLayoutParams(params);
            container.addView(input);

            builder.setView(container);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        float newValue = Float.parseFloat(input.getText().toString());
                        newValue = Math.max(min, Math.min(max, newValue));

                        variableValues.put(key, newValue);
                        valueView.setText(String.format("%.2f", newValue));

                        float newProgress = ((newValue - min) / (max - min)) * 1000f;
                        seekBar.setProgress((int) newProgress);

                        if (listener != null) {
                            listener.onValueChanged(key, newValue);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        public void addButton(String text, View.OnClickListener onClickListener) {
            Button button = new Button(getContext());
            button.setText(text);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            int topMargin = (int) (8 * getResources().getDisplayMetrics().density);
            params.setMargins(0, topMargin, 0, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(onClickListener);
            container.addView(button);
        }

        public void printAllValues() {
            Log.d("ViewDebug", "--- Copy-Pasteable Animation Values ---");
            StringBuilder stringBuilder = new StringBuilder("\n");
            for (Map.Entry<String, Float> entry : variableValues.entrySet()) {
                String key = entry.getKey();
                Float value = entry.getValue();
                if ("diff".equals(key)) {
                    continue;
                }
                stringBuilder.append("private float " + key + " = " + String.format(Locale.US, "%.2f", value) + "f;").append("\n");
            }
            Log.d("ViewDebug", stringBuilder.toString());
            Log.d("ViewDebug", "---------------------------------------");
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (isAnimating) {
                return true;
            }

            final int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    initialRawX = ev.getRawX();
                    initialRawY = ev.getRawY();
                    isDragging = false;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    double dist = Math.sqrt(Math.pow(ev.getRawX() - initialRawX, 2) + Math.pow(ev.getRawY() - initialRawY, 2));
                    if (dist > touchSlop) {
                        float xDiff = Math.abs(ev.getRawX() - initialRawX);
                        float yDiff = Math.abs(ev.getRawY() - initialRawY);

                        if (isExpanded && yDiff > xDiff) {
                            return false;
                        }

                        isDragging = true;
                        dX = getX() - initialRawX;
                        dY = getY() - initialRawY;
                        bringToFront();
                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    isDragging = false;
                    break;
                }
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (isAnimating) {
                return true;
            }

            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    initialRawX = event.getRawX();
                    initialRawY = event.getRawY();
                    isDragging = false;
                    bringToFront();
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (!isDragging) {
                        double dist = Math.sqrt(Math.pow(event.getRawX() - initialRawX, 2) + Math.pow(event.getRawY() - initialRawY, 2));
                        if (dist > touchSlop) {
                            isDragging = true;
                            dX = getX() - initialRawX;
                            dY = getY() - initialRawY;
                        }
                    }

                    if (isDragging) {
                        setX(event.getRawX() + dX);
                        setY(event.getRawY() + dY);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (!isDragging) {
                        toggle();
                    }
                    isDragging = false;
                    break;
                }
            }
            return true;
        }

        private void toggle() {
            if (isExpanded) {
                collapse();
            } else {
                expand();
            }
        }

        private void collapse() {
            if (!isExpanded || isAnimating) return;

            isAnimating = true;

            ValueAnimator sizeAnimator = ValueAnimator.ofInt(expandedWidth, collapsedSize);
            sizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int val = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = getLayoutParams();
                    layoutParams.width = val;
                    layoutParams.height = val;
                    requestLayout();
                }
            });

            ValueAnimator cornerAnimator = ValueAnimator.ofFloat(backgroundDrawable.getCornerRadius(), collapsedSize / 2f);
            cornerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    backgroundDrawable.setCornerRadius((Float) animation.getAnimatedValue());
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    sizeAnimator,
                    cornerAnimator,
                    android.animation.ObjectAnimator.ofFloat(scrollView, "alpha", 1f, 0f),
                    android.animation.ObjectAnimator.ofFloat(settingsButton, "alpha", 0f, 1f)
            );
            animatorSet.setDuration(ANIMATION_DURATION);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isExpanded = false;
                    isAnimating = false;
                    scrollView.setVisibility(View.GONE);
                }
            });
            animatorSet.start();
        }

        private void expand() {
            if (isExpanded || isAnimating) return;

            isAnimating = true;
            scrollView.setVisibility(View.VISIBLE);

            ValueAnimator sizeAnimator = ValueAnimator.ofInt(collapsedSize, expandedWidth);
            sizeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int val = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = getLayoutParams();
                    layoutParams.width = val;
                    if (animation.getAnimatedFraction() > 0.5) {
                        layoutParams.height = (int) (collapsedSize + (expandedHeight - collapsedSize) * animation.getAnimatedFraction());
                    } else {
                        layoutParams.height = val;
                    }
                    requestLayout();
                }
            });

            float cornerRadius = (16 * getResources().getDisplayMetrics().density);
            ValueAnimator cornerAnimator = ValueAnimator.ofFloat(backgroundDrawable.getCornerRadius(), cornerRadius);
            cornerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    backgroundDrawable.setCornerRadius((Float) animation.getAnimatedValue());
                }
            });

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    sizeAnimator,
                    cornerAnimator,
                    android.animation.ObjectAnimator.ofFloat(scrollView, "alpha", 0f, 1f),
                    android.animation.ObjectAnimator.ofFloat(settingsButton, "alpha", 1f, 0f)
            );
            animatorSet.setDuration(ANIMATION_DURATION);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isExpanded = true;
                    isAnimating = false;
                    ViewGroup.LayoutParams layoutParams = getLayoutParams();
                    layoutParams.width = expandedWidth;
                    layoutParams.height = expandedHeight;
                    requestLayout();
                }
            });
            animatorSet.start();
        }
    }
}