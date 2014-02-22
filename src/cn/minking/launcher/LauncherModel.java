package cn.minking.launcher;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import cn.minking.launcher.gadget.GadgetInfo;
import cn.minking.launcher.upsidescene.SceneData;
import android.R.anim;
import android.R.integer;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class LauncherModel extends BroadcastReceiver {
    // 打印LOG的TAG标识
    static final String TAG = "MKHome.Model";
    static final boolean LOGD = true;
    
    private static HashSet<String> sDelayedUpdateBuffer = null;
    // launcher loader 的装载线程
    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private LoaderTask mLoaderTask = null;

    
    private AllAppsList mAllAppsList = null;
    private WeakReference<Callbacks> mCallbacks = null;
    private DeferredHandler mHandler = new DeferredHandler();
    private IconCache mIconCache = null;
    
    private final LauncherApplication mApp;
    
    // 
    private boolean mWorkspaceLoaded = false;
    private boolean mAllAppsLoaded = false;
    
    private boolean mIsLoaderTaskRunning;
    
    private final Object mAllAppsListLock = new Object();
    private final Object mLock = new Object();
    
    // 在引用任何静态bg数据结构之前，这个锁必须锁死。与其他锁不同，这个锁会被长时间持有，我们不希望在第一次装载配置信息的工作线程之外任何bg静态数据结构被引用。
    private static final Object sBgLock = new Object();
    private static final HashMap<Object, byte[]> sBgDbIconCache = new HashMap<Object, byte[]>();
    
    // sBgItemsIdMap maps *all* the ItemInfos (shortcuts, folders, and widgets) created by
    // LauncherModel to their ids
    static final HashMap<Long, ItemInfo> sBgItemsIdMap = new HashMap<Long, ItemInfo>();

    // sBgWorkspaceItems is passed to bindItems, which expects a list of all folders and shortcuts
    //       created by LauncherModel that are directly on the home screen (however, no widgets or
    //       shortcuts within folders).
    static final ArrayList<ItemInfo> sBgWorkspaceItems = new ArrayList<ItemInfo>();

    // sBgAppWidgets is all LauncherAppWidgetInfo created by LauncherModel. Passed to bindAppWidget()
    static final ArrayList<LauncherAppWidgetInfo> sBgAppWidgets = 
            new ArrayList<LauncherAppWidgetInfo>();

    // sBgFolders is all FolderInfos created by LauncherModel. Passed to bindFolders()
    static final HashMap<Long, FolderInfo> sBgFolders = new HashMap<Long, FolderInfo>();
    
    
    private final ArrayList<LauncherAppWidgetInfo> mAppWidgets = new ArrayList<LauncherAppWidgetInfo>();
    private final ArrayList<GadgetInfo> mGadgets = new ArrayList<GadgetInfo>();
    private final HashMap<Long, FolderInfo> mFolders = new HashMap<Long, FolderInfo>();
    private final ArrayList<Object> mItems = new ArrayList<Object>();
    private final HashMap<ComponentName, Long> mLoadedApps = new HashMap<ComponentName, Long>();
    private final HashSet<String> mLoadedPackages = new HashSet<String>();
    private final HashSet<String> mLoadedPresetPackages = new HashSet<String>();
    private final HashSet<String> mLoadedUris = new HashSet<String>();
    
    
    private class LoaderTask implements Runnable{
        private final ContentResolver mContentResolver;
        private Context mContext;
        private HashSet<ComponentName> mInstalledComponents;
        private boolean mIsJustRestoreFinished = false;
        private boolean mIsLaunching = false;
        private boolean mIsLoadingAndBindingWorkspace;
        private boolean mLoadAndBindStepFinished = false;
        private PackageManager mManager = null;
        private boolean mStopped;

        @Override
        public void run() {
            /// LoadTasker run 函数， 执行装载WORKSPACE及APP
            synchronized (mLock) {
                mIsLoaderTaskRunning = true;
            }

            // 对终端用户的使用体验： 如果Launcher运行起来，在前端运行了APP，则首先装载所有的APP。否则，先装载WORKSPACE
            final Callbacks callbacks = mCallbacks.get();
            final boolean loadWorkspaceFirst = callbacks != null ? (!callbacks.isAllAppsVisible()) : true;
            
            keep_running:{
                // 在第一次启动的时候提升装载系统的优先级，防止桌面上什么东西都没有，看起来比较酷
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(mIsLaunching ? Process.THREAD_PRIORITY_DEFAULT : Process.THREAD_PRIORITY_BACKGROUND);
                }
                
                if (loadWorkspaceFirst) {
                    loadAndBindWorkspace();
                }else {
                    loadAndBindAllApps();
                }
                
                if (mStopped) {
                    break keep_running;
                }
                
                // 如果时间较久，那么将装载线程的优先级降下来，先处理UI的线程
                synchronized (mLock) {
                    if (mIsLaunching) {
                        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    }
                }
                
                waitForIdle();
                
                if (loadWorkspaceFirst) {
                    loadAndBindAllApps();
                }else {
                    loadAndBindWorkspace();
                }
                
                synchronized (mLock) {
                    android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                }
            }
            
            synchronized (sBgLock) {
                for (Object keyObject : sBgDbIconCache.keySet()){
                    updateSavedIcon(mContext, (ShortcutInfo)keyObject, sBgDbIconCache.get(keyObject));
                }
                sBgDbIconCache.clear();
            }
        }
        
        public boolean isLaunching() {
            return mIsLaunching;
        }
        
        public boolean isLoadingWorkspace() {
            return mIsLoadingAndBindingWorkspace;
        }
        
        private void waitForIdle() {
            synchronized (LoaderTask.this) {
                
            }
        }
        private void loadAndBindWorkspace(){
            if (!mWorkspaceLoaded) {
                loadWorkspace();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mWorkspaceLoaded = true;
                }
            }
            
            // 绑定 workspace
            bindWorkspace(-1);
        }
        
        private void loadAndBindAllApps() {
            if (!mAllAppsLoaded) {
                loadAllAppsByBatch();
                synchronized (LoaderTask.this) {
                    if (mStopped) {
                        return;
                    }
                    mAllAppsLoaded = true;
                }
            }else {
                onlyBindAllApps();
            }
        }
        
        private void loadWorkspace() {
            final long t = SystemClock.uptimeMillis();
            final Context context = mContext;
            final ContentResolver contentResolver = context.getContentResolver();
            final PackageManager manager = context.getPackageManager();
            final AppWidgetManager widgets = AppWidgetManager.getInstance(context);
            final boolean isSafeMode = manager.isSafeMode();
            
            mApp.getLauncherProvider().loadDefaultFavoritesIfNecessary(0);
            
            synchronized (sBgLock) {
                // MKHOME 桌面项清零重置
                mItems.clear();
                mAppWidgets.clear();
                mFolders.clear();
                mGadgets.clear();
                mLoadedApps.clear();
                mLoadedUris.clear();
                mLoadedPackages.clear();
                mLoadedPresetPackages.clear();
                mInstalledComponents.clear();
                
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                Iterator<ResolveInfo> resolveInfo = mManager.queryIntentActivities(intent, 0).iterator();
                
                int appCounts = 0;
                if (LOGD) Log.d(TAG, "MK : appCounts = " + appCounts);
                
                
                while (resolveInfo.hasNext()) {
                    ResolveInfo rInfo = resolveInfo.next();
                    String packageName = rInfo.activityInfo.packageName;
                    String name = rInfo.activityInfo.name;
                    if (!TextUtils.isEmpty(packageName) && !TextUtils.isEmpty((CharSequence)(name))) {
                        mInstalledComponents.add(new ComponentName(packageName, name));
                        appCounts++;
                        if (LOGD) Log.d(TAG, "MK : pack: " + packageName + "name: " + name);
                    }
                }
                
                if (LOGD) Log.d(TAG, "MK : appCounts = " + appCounts);
                
                if (!mInstalledComponents.isEmpty()) {
                    updateInstalledComponentsArg(mContext);
                }
                
                ArrayList<Object> arrayList = new ArrayList<Object>();
                ArrayList<ItemInfo> arrayList2 = new ArrayList<ItemInfo>();
                Uri joinUri = LauncherSettings.Favorites.getJoinContentUri(" JOIN screens ON favorites.screen=screens._id");
                String aString1[] = ItemQuery.COLUMNS;
                String aString3[] = new String[1];
                aString3[0] = String.valueOf(-100);
                
                loadItems(contentResolver.query(joinUri, aString1, "container=?", aString3, "screens.screenOrder ASC, celly ASC, cellX ASC, itemType ASC"), 
                        arrayList, arrayList2);
                
                Uri uri = LauncherSettings.Favorites.CONTENT_URI;
                loadItems(contentResolver.query(uri, aString1, "container!=?", aString3, null), arrayList, arrayList2);
                ContentProviderClient contentProviderClient = mContentResolver.acquireContentProviderClient(LauncherSettings.Favorites.CONTENT_URI);
                
                if (!arrayList.isEmpty()) {
                    for (Iterator<Object> iterator = arrayList.iterator(); iterator.hasNext();) {
                        long l = ((Long)iterator.next()).longValue();
                        Log.d(TAG, "MK : " + (new StringBuilder()).append("Removed id = ").append(l).toString());
                        try {
                            contentProviderClient.delete(LauncherSettings.Favorites.getContentUri(l), null, null);
                        } catch (RemoteException _ex) {
                            Log.w(TAG, "MK : " + (new StringBuilder()).append("Could not remove id = ").append(l).toString());
                        } catch (SQLException _ex) {
                            Log.w(TAG, "MK : " + (new StringBuilder()).append("Could not remove id(database readonly) = ").append(l).toString());
                        }
                    }
                }
                
                if (!arrayList2.isEmpty()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("screen", Integer.valueOf(-1));
                    
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("_id").append(" IN(");
                    for (Iterator<ItemInfo> iterator = arrayList2.iterator(); 
                            iterator.hasNext();){
                        stringBuilder.append(((ItemInfo)iterator.next()).id).append(',');
                    }
                    
                    try {
                        contentProviderClient.update(LauncherSettings.Favorites.CONTENT_URI, contentValues, stringBuilder.toString(), null);
                    } catch (RemoteException remoteException) {
                        remoteException.printStackTrace();
                    }
                    
                    Iterator<ItemInfo> itemIterator = arrayList2.iterator();
                    while (itemIterator.hasNext()) {
                        ItemInfo itemInfo = itemIterator.next();
                        itemInfo.screenId = -1L;
                        itemInfo.onAddToDatabase(contentValues);
                        try {
                            contentProviderClient.update(LauncherSettings.Favorites.getContentUri(itemInfo.id),
                                    contentValues, null, null);
                            itemInfo.loadPosition(contentValues);
                        } catch (RemoteException remoteException) {
                            remoteException.printStackTrace();
                        }
                        contentValues.clear();
                    }
                }
            }
        }
        
        private void loadItems(Cursor cursor, ArrayList<Object> arrayList, ArrayList<ItemInfo> arrayList2){
            /*while (!mStopped && cursor.moveToNext()) {
                try {
                    int itemType = cursor.getInt(8);
                    switch (itemType) {
                    
                    default:
                        break;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "MK : Desktop items loading interrupted:", e);
                }
                
            }*/
        }
        
        private void updateInstalledComponentsArg(Context context){
            Bundle bundle;
            try {
                bundle = context.getContentResolver().acquireContentProviderClient(LauncherSettings.Favorites.CONTENT_URI).call("updateInstalledComponentsArg", null, null);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        private void bindWorkspace(int synchronizeBindPage) {
            
        }
        
        private void loadAllAppsByBatch(){
            
        }
        
        private void onlyBindAllApps(){
            
        }
        
        public void stopLocked() {
            synchronized (LoaderTask.this) {
                mStopped = true;
                this.notify();
            }
        }
        
        public LoaderTask(Context context, boolean is_launching, boolean is_restore_finished) {
            mInstalledComponents = new HashSet<ComponentName>();
            mContext = context;
            mIsLaunching = is_launching;
            mContentResolver = context.getContentResolver();
            mManager = context.getPackageManager();
            mIsJustRestoreFinished = is_restore_finished;
        }
    }
    
    
    public static interface Callbacks{

        public abstract void bindAppWidget(LauncherAppWidgetInfo launcherappwidgetinfo);

        public abstract void bindAppsAdded(ArrayList arraylist);

        public abstract void bindAppsRemoved(ArrayList arraylist);

        public abstract void bindFolders(HashMap hashmap);

        public abstract void bindGadget(GadgetInfo gadgetinfo);

        public abstract void bindItems(ArrayList arraylist, int i, int j);

        public abstract void bindUpsideScene(SceneData scenedata);

        public abstract void finishBindingMissingItems();

        public abstract void finishBindingSavedItems();

        public abstract void finishLoading();
        
        public boolean isAllAppsVisible();

        public abstract int getCurrentWorkspaceScreen();

        public abstract void reloadWidgetPreview();

        public abstract void startBinding();

        public abstract void startLoading();
    }
    
    
    public LauncherModel(LauncherApplication launcherApplication, IconCache iconCache) {
        mApp = launcherApplication;
        mIconCache = iconCache;
        mHandler = new DeferredHandler();
        mAllAppsList = new AllAppsList();
    }
    
    public void initialize(Callbacks callbacks) {
        synchronized (mLock) {
            mCallbacks = new WeakReference<Callbacks>(callbacks);
        }
    }
    
    public void startLoader(boolean isLaunching, int synchronousBindPage) {
        synchronized (mLock) {
            // 如果线程没有任何作用则不进入调用节省时间及资源
            if (mCallbacks != null && mCallbacks.get() != null) {
                // 如果已经有一个线程在运行则通知停止已经在运行的线程
                isLaunching = isLaunching || stopLoaderLocked();
                
                mLoaderTask = new LoaderTask(mApp, isLaunching, true);
                
                sWorkerThread.setPriority(Thread.NORM_PRIORITY);
                sWorker.post(mLoaderTask);
            }
        }
    }
    
    public void resetLoadedState(boolean resetAllAppsLoaded, boolean resetWorkspaceLoaded) {
        synchronized (mLock) {
            // 首先停止现有的加载器，mAllAppsLoaded及mWorkspaceLoaded设置为false
            stopLoaderLocked();
            if (resetAllAppsLoaded) mAllAppsLoaded = false;
            if (resetWorkspaceLoaded) mWorkspaceLoaded = false;
        }
    }
    
    private boolean stopLoaderLocked() {
        boolean isLaunching = false;
        LoaderTask oldTask = mLoaderTask;
        if (oldTask != null) {
            if (oldTask.isLaunching()) {
                isLaunching = true;
            }
            oldTask.stopLocked();
        }
        return isLaunching;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

    }
    
    public void updateSavedIcon(Context context, ShortcutInfo info, byte[] data) {

    }
    
    /**
     * Update an item to the database in a specified container.
     */
    public static void updateItemInDatabase(Context context, final ItemInfo item) {

    }

}
