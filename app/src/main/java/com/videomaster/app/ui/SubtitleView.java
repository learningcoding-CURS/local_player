package com.videomaster.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Renders subtitle text with a semi-transparent background rounded rectangle.
 * Supports multi-line subtitles.
 */
public class SubtitleView extends View {

    private static final float TEXT_SIZE_SP      = 18f;
    private static final float PADDING_H_DP      = 12f;
    private static final float PADDING_V_DP      = 6f;
    private static final float CORNER_RADIUS_DP  = 6f;
    private static final int   BG_COLOR          = 0xCC000000; // 80% black

    private final TextPaint textPaint;
    private final Paint     bgPaint;
    private final float     paddingH;
    private final float     paddingV;
    private final float     cornerRadius;

    private String       subtitleText;
    private StaticLayout staticLayout;

    public SubtitleView(Context context) {
        this(context, null);
    }

    public SubtitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float density = context.getResources().getDisplayMetrics().density;
        paddingH     = PADDING_H_DP     * density;
        paddingV     = PADDING_V_DP     * density;
        cornerRadius = CORNER_RADIUS_DP * density;

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP,
                context.getResources().getDisplayMetrics()));
        textPaint.setShadowLayer(3f, 1f, 1f, Color.BLACK);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(BG_COLOR);

        setVisibility(GONE);
    }

    /**
     * Sets the subtitle text to display. Pass null or empty string to hide.
     */
    public void setSubtitle(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            subtitleText = null;
            staticLayout = null;
            setVisibility(GONE);
            return;
        }
        if (text.equals(subtitleText)) return;
        subtitleText = text;
        buildLayout();
        setVisibility(VISIBLE);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (subtitleText != null) buildLayout();
    }

    private void buildLayout() {
        int availWidth = (int) (getWidth() - paddingH * 2);
        if (availWidth <= 0) return;
        staticLayout = StaticLayout.Builder
                .obtain(subtitleText, 0, subtitleText.length(), textPaint, availWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.1f)
                .setIncludePad(false)
                .build();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (staticLayout == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int w = staticLayout.getWidth() + (int) (paddingH * 2);
        int h = staticLayout.getHeight() + (int) (paddingV * 2);
        setMeasuredDimension(resolveSize(w, widthMeasureSpec), resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (staticLayout == null) return;
        float bgLeft   = 0;
        float bgTop    = 0;
        float bgRight  = getWidth();
        float bgBottom = getHeight();
        canvas.drawRoundRect(new RectF(bgLeft, bgTop, bgRight, bgBottom),
                cornerRadius, cornerRadius, bgPaint);
        canvas.save();
        canvas.translate(paddingH, paddingV);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /** Changes the subtitle text size in sp. */
    public void setTextSizeSp(float sp) {
        textPaint.setTextSize(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp,
                getContext().getResources().getDisplayMetrics()));
        if (subtitleText != null) buildLayout();
    }
}
