package com.teinvdlugt.android.greekgods;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

public abstract class MyClickableSpan extends ClickableSpan {

    private Context context;

    public MyClickableSpan(Context context) {
        this.context = context;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(false);
        ds.setColor(getColorCompat(context, R.color.hyperlink_text_color));
    }

    public static int getColorCompat(Context context, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(color);
        } else {
            return context.getResources().getColor(color);
        }
    }
}
