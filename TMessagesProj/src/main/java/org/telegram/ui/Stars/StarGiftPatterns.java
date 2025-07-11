package org.telegram.ui.Stars;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;

public class StarGiftPatterns {

    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_ACTION = 1;
    public static final int TYPE_GIFT = 2;
    public static final int TYPE_LINK_PREVIEW = 3;

    private static final float[][] patternLocations = new float[][]{
            {
                    83.33f, 24, 27.33f, .22f,
                    68.66f, 75.33f, 25.33f, .21f,
                    0, 86, 25.33f, .12f,
                    -68.66f, 75.33f, 25.33f, .21f,
                    -82.66f, 13.66f, 27.33f, .22f,
                    -80, -33.33f, 20, .24f,
                    -46.5f, -63.16f, 27, .21f,
                    1, -82.66f, 20, .15f,
                    46.5f, -63.16f, 27, .21f,
                    80, -33.33f, 19.33f, .24f,

                    115.66f, -63, 20, .15f,
                    134, -10.66f, 20, .18f,
                    118.66f, 55.66f, 20, .15f,
                    124.33f, 98.33f, 20, .11f,

                    -128, 98.33f, 20, .11f,
                    -108, 55.66f, 20, .15f,
                    -123.33f, -10.66f, 20, .18f,
                    -116, -63.33f, 20, .15f
            },
            {
                    27.33f, -57.66f, 20, .12f,
                    59, -32, 19.33f, .22f,
                    77, 4.33f, 22.66f, .2f,
                    100, 40.33f, 18, .12f,
                    58.66f, 59, 20, .18f,
                    73.33f, 100.33f, 22.66f, .15f,
                    75, 155, 22, .11f,

                    -27.33f, -57.33f, 20, .12f,
                    -59, -32.33f, 19.33f, .2f,
                    -77, 4.66f, 23.33f, .2f,
                    -98.66f, 41, 18.66f, .12f,
                    -58, 59.33f, 19.33f, .18f,
                    -73.33f, 100, 22, .15f,
                    -75.66f, 155, 22, .11f
            },
            {
                    -0.83f, -52.16f, 12.33f, .2f,
                    26.66f, -40.33f, 16, .2f,
                    44.16f, -20.5f, 12.33f, .2f,
                    53, 7.33f, 16, .2f,
                    31, 23.66f, 14.66f, .2f,
                    0, 32, 13.33f, .2f,
                    -29, 23.66f, 14, .2f,
                    -53, 7.33f, 16, .2f,
                    -44.5f, -20.16f, 12.33f, .2f,
                    -27.33f, -40.33f, 16, .2f,
                    43.66f, 50, 14.66f, .2f,
                    -41.66f, 48, 14.66f, .2f
            },
            {
                    -0.16f, -103.5f, 20.33f, .15f,
                    39.66f, -77.33f, 26.66f, .15f,
                    70.66f, -46.33f, 21.33f, .15f,
                    84.5f, -3.83f, 29.66f, .15f,
                    65.33f, 56.33f, 24.66f, .15f,
                    0, 67.66f, 24.66f, .15f,
                    -65.66f, 56.66f, 24.66f, .15f,
                    -85, -4, 29.33f, .15f,
                    -70.66f, -46.33f, 21.33f, .15f,
                    -40.33f, -77.66f, 26.66f, .15f,

                    62.66f, -109.66f, 21.33f, .11f,
                    103.166f, -67.5f, 20.33f, .11f,
                    110.33f, 37.66f, 20.66f, .11f,
                    94.166f, 91.16f, 20.33f, .11f,
                    38.83f, 91.16f, 20.33f, .11f,
                    0, 112.5f, 20.33f, .11f,
                    -38.83f, 91.16f, 20.33f, .11f,
                    -94.166f, 91.16f, 20.33f, .11f,
                    -110.33f, 37.66f, 20.66f, .11f,
                    -103.166f, -67.5f, 20.33f, .11f,
                    -62.66f, -109.66f, 21.33f, .11f
            }
    };

    public static void drawPattern(Canvas canvas, Drawable pattern, float w, float h, float alpha, float scale) {
        drawPattern(canvas, TYPE_DEFAULT, pattern, w, h, alpha, scale);
    }

    public static void drawPattern(Canvas canvas, int type, Drawable pattern, float w, float h, float alpha, float scale) {
        if (alpha <= 0.0f) return;
        for (int i = 0; i < patternLocations[type].length; i += 4) {
            final float x = patternLocations[type][i];
            final float y = patternLocations[type][i + 1];
            final float size = patternLocations[type][i + 2];
            final float thisAlpha = patternLocations[type][i + 3];

            float cx = x, cy = y, sz = size;
            if (w < h && type == TYPE_DEFAULT) {
                cx = y;
                cy = x;
            }
            cx *= scale;
            cy *= scale;
            sz *= scale;
            pattern.setBounds((int) (dp(cx) - dp(sz) / 2.0f), (int) (dp(cy) - dp(sz) / 2.0f), (int) (dp(cx) + dp(sz) / 2.0f), (int) (dp(cy) + dp(sz) / 2.0f));

            pattern.setAlpha((int) (0xFF * alpha * thisAlpha));
            pattern.draw(canvas);
        }
    }

    private static final float[] profileRight = new float[]{
            -35.66f, -5, 24, .2388f,
            -14.33f, -29.33f, 20.66f, .32f,
            -15, -73.66f, 19.33f, .32f,
            -2, -99.66f, 18, .1476f,
            -64.33f, -24.66f, 23.33f, .3235f,
            -40.66f, -53.33f, 24, .3654f,
            -50.33f, -85.66f, 20, .172f,
            -96, -1.33f, 19.33f, .3343f,
            -136.66f, -13, 18.66f, .2569f,
            -104.66f, -33.66f, 20.66f, .2216f,
            -82, -62.33f, 22.66f, .2562f,
            -131.66f, -60, 18, .1316f,
            -105.66f, -88.33f, 18, .1487f
    };
    private static final float[] profileLeft = new float[]{
            0, -107.33f, 16, .1505f,
            14.33f, -84, 18, .1988f,
            0, -50.66f, 18.66f, .3225f,
            13, -15, 18.66f, .37f,
            43.33f, 1, 18.66f, .3186f
    };

    public static void drawProfilePattern(Canvas canvas, Drawable pattern, float w, float h, float alpha, float full) {
        if (alpha <= 0.0f) return;

        final float b = h;
        final float l = 0, r = w;

        if (full > 0) {
            for (int i = 0; i < profileLeft.length; i += 4) {
                final float x = profileLeft[i];
                final float y = profileLeft[i + 1];
                final float size = profileLeft[i + 2];
                final float thisAlpha = profileLeft[i + 3];

                pattern.setBounds(
                        (int) (l + dpf2(x) - dpf2(size) / 2.0f),
                        (int) (b + dpf2(y) - dpf2(size) / 2.0f),
                        (int) (l + dpf2(x) + dpf2(size) / 2.0f),
                        (int) (b + dpf2(y) + dpf2(size) / 2.0f)
                );
                pattern.setAlpha((int) (0xFF * alpha * thisAlpha * full));
                pattern.draw(canvas);
            }

            final float sl = 77.5f, sr = 173.33f;
            final float space = w / AndroidUtilities.density - sl - sr;
            int count = Math.max(0, Math.round(space / 27.25f));
            if (count % 2 == 0) {
                count++;
            }
            for (int i = 0; i < count; ++i) {
                final float x = sl + space * ((float) i / (count - 1));
                final float y = i % 2 == 0 ? 0 : -12.5f;
                final float size = 17;
                final float thisAlpha = .21f;

                pattern.setBounds(
                        (int) (l + dpf2(x) - dpf2(size) / 2.0f),
                        (int) (b + dpf2(y) - dpf2(size) / 2.0f),
                        (int) (l + dpf2(x) + dpf2(size) / 2.0f),
                        (int) (b + dpf2(y) + dpf2(size) / 2.0f)
                );
                pattern.setAlpha((int) (0xFF * alpha * thisAlpha * full));
                pattern.draw(canvas);
            }
        }

        for (int i = 0; i < profileRight.length; i += 4) {
            final float x = profileRight[i];
            final float y = profileRight[i + 1];
            final float size = profileRight[i + 2];
            final float thisAlpha = profileRight[i + 3];

            pattern.setBounds(
                    (int) (r + dpf2(x) - dpf2(size) / 2.0f),
                    (int) (b + dpf2(y) - dpf2(size) / 2.0f),
                    (int) (r + dpf2(x) + dpf2(size) / 2.0f),
                    (int) (b + dpf2(y) + dpf2(size) / 2.0f)
            );
            pattern.setAlpha((int) (0xFF * alpha * thisAlpha));
            pattern.draw(canvas);
        }
    }

    private static final float[][] radialPositions = {
            {-0.89f, -1.52f},
            {0.04f, -1.30f},
            {1.04f, -1.54f},
            {-2.04f, -1.04f},
            {-1.16f, -0.67f},
            {1.36f, -0.62f},
            {2.15f, -1.14f},
            {-2.83f, 0.17f},
            {-1.78f, 0.17f},
            {1.94f, 0.12f},
            {3.01f, 0.13f},
            {-1.21f, 0.88f},
            {1.33f, 0.96f},
            {-2.17f, 1.36f},
            {-0.88f, 1.83f},
            {0.05f, 1.38f},
            {1.04f, 1.78f},
            {2.24f, 1.39f},
    };

    public static void drawRadialPattern(Canvas canvas, Drawable pattern, View avatarContainer, float alpha, float fraction) {
        // debug params
        FileLog.d(String.format("drawRadialPattern called with alpha=%f and fraction=%f", alpha, fraction));

        if (fraction <= 0f || alpha <= 0f || pattern == null || avatarContainer == null) {
            return;
        }

        float avatarRadius = avatarContainer.getMeasuredWidth() / 2f;
        if (avatarRadius <= 0) {
            avatarRadius = avatarContainer.getWidth() / 2f;
        }
        if (avatarRadius <= 0) {
            return;
        }
        final float avatarCx = avatarContainer.getX() + avatarRadius;
        final float avatarCy = avatarContainer.getY() + avatarRadius;

        float maxDist = 0f;
        final float[][] finalPositions = new float[radialPositions.length][2];
        final float[] distances = new float[radialPositions.length];

        for (int i = 0; i < radialPositions.length; i++) {
            final float[] position = radialPositions[i];
            final float posXMultiplier = position[0];
            final float posYMultiplier = position[1];

            final float targetX = avatarCx + posXMultiplier * avatarRadius;
            final float targetY = avatarCy + posYMultiplier * avatarRadius;

            final float vecX = targetX - avatarCx;
            final float vecY = targetY - avatarCy;
            final float distFromCenter = (float) Math.sqrt(vecX * vecX + vecY * vecY);

            final float minAllowedDist = avatarRadius + dp(10f);

            float finalX = targetX;
            float finalY = targetY;

            if (distFromCenter < minAllowedDist) {
                if (distFromCenter > 0) {
                    finalX = avatarCx + (vecX / distFromCenter) * minAllowedDist;
                    finalY = avatarCy + (vecY / distFromCenter) * minAllowedDist;
                } else {
                    finalX = avatarCx + minAllowedDist;
                    finalY = avatarCy;
                }
            }
            finalPositions[i][0] = finalX;
            finalPositions[i][1] = finalY;

            final float finalVecX = finalX - avatarCx;
            final float finalVecY = finalY - avatarCy;
            distances[i] = (float) Math.sqrt(finalVecX * finalVecX + finalVecY * finalVecY);
            if (distances[i] > maxDist) {
                maxDist = distances[i];
            }
        }

        if (maxDist <= 0) {
            maxDist = 1f;
        }

        final float startX = avatarCx;
        final float startY = avatarCy;
        final float spread = 1.5f;

        for (int i = 0; i < radialPositions.length; i++) {
            final float dist = distances[i];
            final float normDist = dist / maxDist;

            final float exponent = 1.0f + spread * (1.0f - normDist);
            final float easedProgress = (float) Math.pow(fraction, exponent);

            if (easedProgress <= 0.001f) {
                continue;
            }

            final float finalX = finalPositions[i][0];
            final float finalY = finalPositions[i][1];

            final float currentX = AndroidUtilities.lerp(startX, finalX, easedProgress);
            final float currentY = AndroidUtilities.lerp(startY, finalY, easedProgress);

            final float[] position = radialPositions[i];
            final float posXMultiplier = position[0];
            final float posYMultiplier = position[1];
            final float distMultiplier = (float) Math.sqrt(posXMultiplier * posXMultiplier + posYMultiplier * posYMultiplier);
            final float finalSize = dp(24f) * (1.0f - distMultiplier * 0.1f);
            final float currentSize = finalSize * easedProgress;

            if (currentSize <= 1) {
                continue;
            }

            final int currentAlpha = (int) (255 * alpha * 0.3f * easedProgress);
            pattern.setAlpha(currentAlpha);

            pattern.setBounds(
                    (int) (currentX - currentSize / 2f),
                    (int) (currentY - currentSize / 2f),
                    (int) (currentX + currentSize / 2f),
                    (int) (currentY + currentSize / 2f)
            );
            pattern.draw(canvas);
        }
    }
}
