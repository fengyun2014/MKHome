package cn.minking.launcher;
/**
 * 作者： minking
 * 描述：MKHome的主入口， 装载Launcher.xml及LauncherModel的调用
 * 更新内容
 * ====================================================================================
 * 2014-02-18： 实现LauncherModel读取手机中的APP及WIDGET
 * ====================================================================================
 */
import java.util.ArrayList;
import java.util.HashMap;

import cn.minking.launcher.gadget.GadgetInfo;
import cn.minking.launcher.upsidescene.SceneData;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;

public class Launcher extends Activity implements OnClickListener,
        OnLongClickListener, LauncherModel.Callbacks{
    static final private String LTAG = "ZwLauncher";
    private Workspace mWorkspace = null;
    private DragLayer mDragLayer = null;
    private DragController mDragController = null;
    private LauncherModel mModel = null;
    private static boolean mIsHardwareAccelerated = false;
    private IconCache mIconCache = null;
    private Point mTmpPoint = new Point();
    
    static final int APPWIDGET_HOST_ID = 1024;
    
    /// M: 静态变量标识本地信息是否变更
    private static boolean sLocaleChanged = false;
    
    private boolean mPaused = true;
    private boolean mRestoring = false;
    private boolean mWaitingForResult;
    private boolean mOnResumeNeedsLoad;

    /// M: 用于强制重载WORKSPACE
    private boolean mIsLoadingWorkspace;
    

    /// M: 跟踪用户是否离开Launcher的行为状态
    private static boolean sPausedFromUserAction = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LTAG, "onCreate");
        Window localWindow = getWindow();
        localWindow.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        localWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //mIsHardwareAccelerated = ((Window)(localWindow)).getWindowManager().isHardwareAccelerated();
        LauncherApplication launcherApplication = (LauncherApplication)getApplication();
        mModel = launcherApplication.setLauncher(this);
        mIconCache = launcherApplication.getIconCache();
        mDragController = new DragController();
        registerContentObservers();
        setWallpaperDimension();
        
        
        mPaused = false;
        
        setContentView(R.layout.launcher);
        setupViews();
        
        if (!mRestoring) {
            /// M: 如果本地信息变更了，设置为重新装载所有信息
            if (sLocaleChanged) {
                mModel.resetLoadedState(true, true);
                sLocaleChanged = false;
            }
            mIsLoadingWorkspace = true;
            if (sPausedFromUserAction) {
                // 如果用户离开了launcher， 只需要在回到launcher的时候异步的完成装载
                mModel.startLoader(true, -1);
            } else {
                // 如果用户旋转屏幕或更改配置，则同步装载
                mModel.startLoader(true, mWorkspace.getCurrentPage());
            }
        }
    }

    private void registerContentObservers(){
        ContentResolver contentResolver = getContentResolver();
        //contentResolver.registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true, mWidgetObserver);
        //contentResolver.registerContentObserver(LauncherSettings.Screens.CONTENT_URI, true, mScreenChangeObserver);
    }
    
    private void setWallpaperDimension() {
        WallpaperManager wallpaperManager = (WallpaperManager)getSystemService("wallpaper");
        Display display = getWindowManager().getDefaultDisplay();
        int rotation = display.getRotation();
        int width = 0;
        int height = 0;
        boolean bFlag = false;
        
        if (rotation != Surface.ROTATION_0 && rotation != Surface.ROTATION_180) {
            bFlag = false;
        }else {
            bFlag = true;
        }
        display.getSize(mTmpPoint);
        
        if (!bFlag) {
            width = mTmpPoint.y;
            height = mTmpPoint.x;
        }else {
            width = mTmpPoint.x;
            height = mTmpPoint.y;
        }
        wallpaperManager.suggestDesiredDimensions(width * 2, height);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        
        mPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        
        mPaused = false;
        
        sPausedFromUserAction = false;
        
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWorkspace.onStart();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        sPausedFromUserAction = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onLongClick(View paramView) {
        return false;
    }

    @Override
    public void onClick(View paramView) {
        // TODO Auto-generated method stub

    }
    
    public void startActivityEx(Intent intent, Bundle options) {
        startActivity(intent, options);
    }
    
    @Override
    public void startActivity(Intent intent, Bundle options) {
        super.startActivity(intent);
    }

    private void setupViews() {
        DragController dragController = mDragController;
        mDragLayer = (DragLayer)findViewById(R.id.drag_layer);
        mWorkspace = (Workspace)mDragLayer.findViewById(R.id.workspace);
        Workspace workspace = mWorkspace;
        workspace.setHapticFeedbackEnabled(false);
        workspace.setOnLongClickListener(this);
        workspace.setDragController(dragController);
        workspace.setLauncher(this);
    }

    @Override
    public void bindAppWidget(LauncherAppWidgetInfo launcherappwidgetinfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void bindAppsAdded(ArrayList arraylist) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void bindAppsRemoved(ArrayList arraylist) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void bindFolders(HashMap hashmap) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void bindGadget(GadgetInfo gadgetinfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void bindItems(ArrayList arraylist, int i, int j) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void bindUpsideScene(SceneData scenedata) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isAllAppsVisible() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void finishBindingMissingItems() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finishBindingSavedItems() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void finishLoading() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getCurrentWorkspaceScreen() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void reloadWidgetPreview() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void startBinding() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void startLoading() {
        // TODO Auto-generated method stub
        
    }

    
}
