package hoo.etahk.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * Source: https://medium.com/@nullthemall/bottomnavigationview-missing-pearls-eaa950f9ad4e
 * TODO("Convert to Kotlin")
 */
public final class BottomNavigationBehavior<V extends View> extends VerticalScrollingBehavior<V> {
    private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();
    private int mTabLayoutId;
    private boolean hidden = false;
    private boolean hiddenAfterSnackbar = false;
    private ViewPropertyAnimatorCompat mOffsetValueAnimator;
    private ViewGroup mTabLayout;
    private int mSnackbarHeight = -1;
    private boolean scrollingEnabled = true;

    int[] attrsArray = new int[]{
            android.R.attr.id, android.R.attr.elevation};
    private int mElevation = 8;

    public BottomNavigationBehavior() {
        super();
    }

    public BottomNavigationBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                attrsArray);
        mTabLayoutId = a.getResourceId(0, View.NO_ID);
        mElevation = a.getResourceId(1, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mElevation, context.getResources().getDisplayMetrics()));
        a.recycle();
    }

    public static <V extends View> BottomNavigationBehavior<V> from(@NonNull V view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof BottomNavigationBehavior)) {
            throw new IllegalArgumentException(
                    "The view is not associated with BottomNavigationBehavior");
        }
        return (BottomNavigationBehavior<V>) behavior;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        updateSnackbar(parent, dependency, child);
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    private void updateSnackbar(CoordinatorLayout parent, View dependency, View child) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            if (mSnackbarHeight == -1) {
                mSnackbarHeight = dependency.getHeight();
            }
            int targetPadding = (mSnackbarHeight +
                    (hidden ? 0 : child.getMeasuredHeight()));

            dependency.setPadding(dependency.getPaddingLeft(),
                    dependency.getPaddingTop(), dependency.getPaddingRight(), targetPadding
            );
        }
    }

    @Override
    public void onDependentViewRemoved(CoordinatorLayout parent, V child, View dependency) {
        updateScrollingForSnackbar(dependency, child, true);
        super.onDependentViewRemoved(parent, child, dependency);
    }

    private void updateScrollingForSnackbar(View dependency, V child, boolean enabled) {
        if (dependency instanceof Snackbar.SnackbarLayout) {
            scrollingEnabled = enabled;
            if (!scrollingEnabled) return;

            if (this.mTotalDyUnconsumed < 0 && hidden) {
                hidden = false;
                animateOffset(child, 0);
            } else if (this.mTotalDyUnconsumed > 0 && !hidden) {
                hidden = true;
                animateOffset(child, child.getHeight());
            }
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        updateScrollingForSnackbar(dependency, child, false);
        return super.onDependentViewChanged(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        boolean layoutChild = super.onLayoutChild(parent, child, layoutDirection);
        if (mTabLayout == null && mTabLayoutId != View.NO_ID) {
            mTabLayout = findTabLayout(child);
            elevateNavigationView();
        }

        return layoutChild;
    }

    @Nullable
    private ViewGroup findTabLayout(@NonNull View child) {
        if (mTabLayoutId == 0) return null;
        return (ViewGroup) child.findViewById(mTabLayoutId);
    }

    @Override
    public void onNestedVerticalOverScroll(CoordinatorLayout coordinatorLayout, V child, @ScrollDirection int direction, int currentOverScroll, int totalOverScroll) {
    }

    @Override
    public void onDirectionNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed, @ScrollDirection int scrollDirection) {
        handleDirection(child, scrollDirection);
    }

    private void handleDirection(V child, @ScrollDirection int scrollDirection) {
        //if (!scrollingEnabled) return;
        if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_DOWN && hidden) {
            if (!scrollingEnabled) { hiddenAfterSnackbar = false; return;}
            hidden = false;
            animateOffset(child, 0);
        } else if (scrollDirection == ScrollDirection.SCROLL_DIRECTION_UP && !hidden) {
            if (!scrollingEnabled) { hiddenAfterSnackbar = true; return;}
            hidden = true;
            animateOffset(child, child.getHeight());
        }
    }

    @Override
    protected boolean onNestedDirectionFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY, @ScrollDirection int scrollDirection) {
        handleDirection(child, scrollDirection);
        return true;
    }

    private void animateOffset(final V child, final int offset) {
        ensureOrCancelAnimator(child);
        mOffsetValueAnimator.translationY(offset).start();
    }

    private void ensureOrCancelAnimator(@NonNull  V child) {
        if (mOffsetValueAnimator == null) {
            mOffsetValueAnimator = ViewCompat.animate(child);
            mOffsetValueAnimator.setDuration(300);
            mOffsetValueAnimator.setInterpolator(INTERPOLATOR);
        } else {
            mOffsetValueAnimator.cancel();
        }
    }

    private void elevateNavigationView() {
        if (mTabLayout != null) {
            ViewCompat.setElevation(mTabLayout, mElevation);
        }
    }

    public boolean isScrollingEnabled() {
        return scrollingEnabled;
    }

    public void setScrollingEnabled(boolean scrollingEnabled) {
        this.scrollingEnabled = scrollingEnabled;
    }

    public void setHidden(V view, boolean bottomLayoutHidden) {
        if (!bottomLayoutHidden && hidden) {
            animateOffset(view, 0);
        } else if (bottomLayoutHidden && !hidden) {
            animateOffset(view, -view.getHeight());
        }
        hidden = bottomLayoutHidden;
    }
}