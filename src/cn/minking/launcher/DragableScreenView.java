package cn.minking.launcher;
/**
 * 作者：      minking
 * 文件名称:    DragableScreenView.java
 * 创建时间：    2013
 * 描述：  
 * 更新内容
 * ====================================================================================
 * 20140228: 拖动显示Screen View类
 * 20140317: 完成屏幕触摸状态判断
 * ====================================================================================
 */
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DragableScreenView extends ScreenView
    implements DragScroller{
    private static final String TAG = "MKHome.DragableScreenView";
    
    /******* 常量 *******/
    // 滑动方向
    public final static int DIRECTION_LEFT = 0;
    public final static int DIRECTION_RIGHT = 1;
    
    public final static int DRAG_IN_SCREEN = 0;
    public final static int DRAG_ATTACH_EDGE = 1;
    
    // 当手指移动到屏幕的边缘，需要切换至下一个屏幕
    private int mScrollZone = 30;

    /******* 状态 *******/
    protected int mDragScrollState;
    
    /******* 动作 *******/
    protected Handler mDragScrollHandler;
    protected ScrollRunnable mDragScrollRunnable;
    

    /**
     * 功能： 使用线程来处理屏幕的左右滑动
     * @author minking
     *
     */
    protected class ScrollRunnable
        implements Runnable {
        private int mDirection;
    
        @Override
        public void run() {
            if (mDirection == DIRECTION_LEFT){
                scrollDragingLeft();
            } else {
                scrollDragingRight();
            }
            mDragScrollState = DRAG_IN_SCREEN;
        }
 
        public void setDirection(int i) {
            mDirection = i;
        }
    }

    public DragableScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragableScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScrollZone = context.getResources().getDimensionPixelSize(R.dimen.scroll_zone);
        mDragScrollState = DRAG_IN_SCREEN;
        mDragScrollRunnable = new ScrollRunnable();
        mDragScrollHandler = new Handler();
    }
    
    /**
     * 功能： 向左滑动
     */
    @Override
    public void scrollDragingLeft() {
        if (!mScroller.isFinished()) {
            if (mNextScreen > 0) {
                snapToScreen(mNextScreen - 1);
            }
        } else {
            if (mCurrentScreen > 0){
                snapToScreen(mCurrentScreen - 1);
            }
        }
    }

    /**
     * 功能： 向右滑动
     */
    @Override
    public void scrollDragingRight() {
        if (!mScroller.isFinished()) {
            if (mNextScreen < (getScreenCount() - 1)){
                snapToScreen(mNextScreen + 1);
            }
        } else {
            if (mCurrentScreen < (getScreenCount() - 1)){
                snapToScreen(mCurrentScreen + 1);
            }
        }
    }
    
    /**
     * 功能： 触摸功能
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        int direction = DIRECTION_LEFT;
        switch (MotionEvent.ACTION_MASK & ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_MOVE:
            // 只处理触摸在拖动物体时
            if (getTouchState() == TOUCH_STATE_DRAG) {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                int x = (int)ev.getX(pointerIndex);
                
                // 如果手指滑动至屏幕边缘
                if (x >= mScrollZone && x <= (getWidth() - mScrollZone)) {
                    if (mDragScrollState == DRAG_ATTACH_EDGE) {
                        mDragScrollState = DRAG_IN_SCREEN;
                        mDragScrollHandler.removeCallbacks(mDragScrollRunnable);
                    }
                } else {                
                    if (mDragScrollState == DRAG_IN_SCREEN) {
                        mDragScrollState = DRAG_ATTACH_EDGE;
                        ScrollRunnable scrollrunnable = mDragScrollRunnable;
                        if (x >= mScrollZone){
                            direction = DIRECTION_RIGHT;
                        }
                        scrollrunnable.setDirection(direction);
                        mDragScrollHandler.postDelayed(mDragScrollRunnable, 600L);
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            break;
        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return true;
    }
    
    @Override
    public boolean onEnterScrollArea(int i, int j, int k) {
        return true;
    }

    @Override
    public boolean onExitScrollArea() {
        return true;
    }


    @Override
    public void onSecondaryPointerDown(MotionEvent motionEvent, int point_id) {
        super.onSecondaryPointerDown(motionEvent, point_id);
    }

    @Override
    public void onSecondaryPointerMove(MotionEvent motionEvent, int point_id) {
        super.onSecondaryPointerMove(motionEvent, point_id);
    }

    @Override
    public void onSecondaryPointerUp(MotionEvent motionEvent, int point_id) {
        super.onSecondaryPointerUp(motionEvent, point_id);
    }
}
