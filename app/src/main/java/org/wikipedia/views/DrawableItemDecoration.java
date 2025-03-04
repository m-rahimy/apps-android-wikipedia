package org.wikipedia.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import org.wikipedia.util.ResourceUtil;

// todo: replace with DividerItemDecoration once it supports headers and footers
public class DrawableItemDecoration extends RecyclerView.ItemDecoration {
    @NonNull private final Drawable drawable;
    private final boolean drawStart;
    private final boolean drawEnd;

    public DrawableItemDecoration(@NonNull Context context, @AttrRes int id) {
        this(context, id, true, true);
    }

    public DrawableItemDecoration(@NonNull Context context, @AttrRes int id, boolean drawStart, boolean drawEnd) {
        this.drawable = AppCompatResources.getDrawable(context,
                ResourceUtil.getThemedAttributeId(context, id));
        this.drawStart = drawStart;
        this.drawEnd = drawEnd;
    }

    @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.top = drawable.getIntrinsicHeight();
        if (parent.getChildAdapterPosition(view) == state.getItemCount() - 1) {
            outRect.bottom = drawable.getIntrinsicHeight();
        }
    }

    @Override public void onDraw(Canvas canvas, @NonNull RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        if (parent.getChildCount() == 0) {
            return;
        }

        int end = parent.getChildCount() - 1;
        for (int i = drawStart ? 0 : 1; i < end; ++i) {
            draw(canvas, bounds(parent, parent.getChildAt(i), true));
        }
        draw(canvas, bounds(parent, parent.getChildAt(end), true));
        if (drawEnd) {
            draw(canvas, bounds(parent, parent.getChildAt(end), false));
        }
    }

    private Rect bounds(@NonNull RecyclerView parent, @NonNull View child, boolean top) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        Rect bounds = new Rect();
        bounds.right = parent.getWidth() - parent.getPaddingRight();
        bounds.left = parent.getPaddingLeft();
        int height = drawable.getIntrinsicHeight();
        bounds.top = top
                ? layoutManager.getDecoratedTop(child)
                : layoutManager.getDecoratedBottom(child) - height;
        bounds.bottom = bounds.top + height;

        return bounds;
    }

    private void draw(Canvas canvas, @NonNull Rect bounds) {
        drawable.setBounds(bounds);
        drawable.draw(canvas);
    }
}
