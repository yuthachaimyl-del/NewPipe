package org.schabi.newpipe.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import org.schabi.newpipe.R;

/**
 * Separate UI scaling for ultra-wide car head units.
 * <ul>
 *     <li>Home grid: fixed column count (res/values/car_ui.xml), thumbnails shrink to fit.</li>
 *     <li>Toolbar: taller bar and finger-sized buttons, independent of the system DPI.</li>
 * </ul>
 */
public final class CarUi {
    private CarUi() { }

    /**
     * Landscape screen whose width/height is at least car_min_aspect_ratio
     * (e.g. 1920x720 = 2.67).
     */
    public static boolean isCarScreen(@NonNull final Context context) {
        final Resources res = context.getResources();
        if (res.getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        final DisplayMetrics dm = res.getDisplayMetrics();
        final TypedValue tv = new TypedValue();
        res.getValue(R.dimen.car_min_aspect_ratio, tv, true);
        return dm.heightPixels > 0 && (float) dm.widthPixels / dm.heightPixels >= tv.getFloat();
    }

    /**
     * Grid columns: on a car screen always car_grid_span_count (5);
     * elsewhere the normal "as many as fit" rule.
     */
    public static int gridSpanCount(@NonNull final Context context, final int minItemWidthPx) {
        if (isCarScreen(context)) {
            return context.getResources().getInteger(R.integer.car_grid_span_count);
        }
        final int width = context.getResources().getDisplayMetrics().widthPixels;
        return Math.max(1, width / Math.max(1, minItemWidthPx));
    }

    /**
     * Makes the toolbar taller and its buttons finger-sized. Safe to call repeatedly:
     * the drawer toggle and fragments swap the navigation button, so this is re-applied
     * on every layout pass.
     */
    public static void enlargeToolbar(@NonNull final Toolbar toolbar) {
        if (!isCarScreen(toolbar.getContext())) {
            return;
        }
        final Resources res = toolbar.getResources();
        final int barHeight = res.getDimensionPixelSize(R.dimen.car_toolbar_height);
        final ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
        if (lp != null && lp.height != barHeight) {
            lp.height = barHeight;
            toolbar.setLayoutParams(lp);
        }
        toolbar.setMinimumHeight(barHeight);
        applyToButtons(toolbar);
        toolbar.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) ->
                applyToButtons(toolbar));
    }

    /** The navigation (hamburger / back) button is an ImageButton child of the Toolbar. */
    private static void applyToButtons(@NonNull final Toolbar toolbar) {
        final Resources res = toolbar.getResources();
        final int size = res.getDimensionPixelSize(R.dimen.car_toolbar_button_size);
        final int icon = res.getDimensionPixelSize(R.dimen.car_toolbar_icon_size);
        final int pad = (size - icon) / 2;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            final View child = toolbar.getChildAt(i);
            if (child instanceof ImageButton && child.getLayoutParams().width != size) {
                final ViewGroup.LayoutParams p = child.getLayoutParams();
                p.width = size;
                p.height = size;
                child.setLayoutParams(p);
                child.setPadding(pad, pad, pad, pad);
                ((ImageButton) child).setScaleType(ImageButton.ScaleType.FIT_CENTER);
                child.setMinimumWidth(size);
                child.setMinimumHeight(size);
            }
        }
    }
}
