package hoo.etahk.common.view;

import android.content.Context;
import android.os.Parcelable;
import androidx.annotation.IntDef;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Source: https://medium.com/@nullthemall/bottomnavigationview-missing-pearls-eaa950f9ad4e
 * TODO("Convert to Kotlin")
 */
public abstract class VerticalScrollingBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

    private static final int dyIgnore = -1;

    protected int mTotalDyUnconsumed = 0;
    private int mTotalDy = 0;
    @ScrollDirection
    private int mOverScrollDirection = ScrollDirection.SCROLL_NONE;
    @ScrollDirection
    private int mScrollDirection = ScrollDirection.SCROLL_NONE;

    public VerticalScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalScrollingBehavior() {
        super();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ScrollDirection.SCROLL_DIRECTION_UP, ScrollDirection.SCROLL_DIRECTION_DOWN})
    public @interface ScrollDirection {
        int SCROLL_DIRECTION_UP = 1;
        int SCROLL_DIRECTION_DOWN = -1;
        int SCROLL_NONE = 0;
    }

    /**
       @return Overscroll direction: SCROLL_DIRECTION_UP, CROLL_DIRECTION_DOWN, SCROLL_NONE
    */
    @ScrollDirection
    public int getOverScrollDirection() {
        return mOverScrollDirection;
    }


    /**
     * @return Scroll direction: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN, SCROLL_NONE
     */
    @ScrollDirection
    public int getScrollDirection() {
        return mScrollDirection;
    }


    /**
     * @param coordinatorLayout CoordinatoyLayout parent
     * @param child             View child
     * @param direction         Direction of the overscroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     * @param currentOverScroll Unconsumed value, negative or positive based on the direction;
     * @param totalOverScroll   Cumulative value for current direction
     */
    public abstract void onNestedVerticalOverScroll(CoordinatorLayout coordinatorLayout, V child, @ScrollDirection int direction, int currentOverScroll, int totalOverScroll);

    /**
     * @param scrollDirection Direction of the overscroll: SCROLL_DIRECTION_UP, SCROLL_DIRECTION_DOWN
     */
    public abstract void onDirectionNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed, @ScrollDirection int scrollDirection);

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes, int type) {
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes, type);
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int type) {
        super.onStopNestedScroll(coordinatorLayout, child, target, type);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        if (Math.abs(dyConsumed) > dyIgnore)
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (dyUnconsumed > 0 && mTotalDyUnconsumed < 0) {
            mTotalDyUnconsumed = 0;
            mOverScrollDirection = ScrollDirection.SCROLL_DIRECTION_UP;
        } else if (dyUnconsumed < 0 && mTotalDyUnconsumed > 0) {
            mTotalDyUnconsumed = 0;
            mOverScrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN;
        }
        else {
            mOverScrollDirection = ScrollDirection.SCROLL_NONE;
        }
        mTotalDyUnconsumed += dyUnconsumed;
        if (Math.abs(dyConsumed) > dyIgnore && mOverScrollDirection != ScrollDirection.SCROLL_NONE)
            onNestedVerticalOverScroll(coordinatorLayout, child, mOverScrollDirection, dyConsumed, mTotalDyUnconsumed);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed, int type) {
        if (Math.abs(dy) > dyIgnore)
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        if (dy > 0 && mTotalDy < 0) {
            mTotalDy = 0;
            mScrollDirection = ScrollDirection.SCROLL_DIRECTION_UP;
        } else if (dy < 0 && mTotalDy > 0) {
            mTotalDy = 0;
            mScrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN;
        }
        else {
            mScrollDirection = ScrollDirection.SCROLL_NONE;
        }
        mTotalDy += dy;
        if (Math.abs(dy) > dyIgnore && mScrollDirection != ScrollDirection.SCROLL_NONE)
            onDirectionNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, mScrollDirection);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY, boolean consumed) {
        super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
        if (velocityY > 0) {
            mScrollDirection = ScrollDirection.SCROLL_DIRECTION_UP;
        } else if (velocityY < 0) {
            mScrollDirection = ScrollDirection.SCROLL_DIRECTION_DOWN;
        } else {
            mScrollDirection = ScrollDirection.SCROLL_NONE;
        }
        return onNestedDirectionFling(coordinatorLayout, child, target, velocityX, velocityY, mScrollDirection);
    }

    protected abstract boolean onNestedDirectionFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY, @ScrollDirection int scrollDirection);

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @Override
    public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, V child, WindowInsetsCompat insets) {

        return super.onApplyWindowInsets(coordinatorLayout, child, insets);
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
        return super.onSaveInstanceState(parent, child);
    }

}