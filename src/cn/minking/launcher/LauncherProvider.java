package cn.minking.launcher;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.appwidget.AppWidgetHost;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

public class LauncherProvider extends ContentProvider {
    private static final String TAG = "MKHome.Provider";
    private static final boolean LOGD = true;
    
    // 用于存储的数据库名称及版本号
    private static final String DATABASE_NAME = "mkhome.db";
    private static final int DATABASE_VERSION = 1;

    static final String AUTHORITY = "cn.minking.launcher.settings";
    
    static final Uri CONTENT_APPWIDGET_RESET_URI =
            Uri.parse("content://" + AUTHORITY + "/appWidgetReset");
    
    static final String TABLE_FAVORITES = "favorites";
    static final String PARAMETER_NOTIFY = "notify";
    static final String DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED =
            "DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED";
    static final String DEFAULT_WORKSPACE_RESOURCE_ID =
            "DEFAULT_WORKSPACE_RESOURCE_ID";
    
    /// M: 保存场景数据
    private static DatabaseHelper sOpenHelper;
    
    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }
    
    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCreate() {
        sOpenHelper = new DatabaseHelper(getContext());
        ((LauncherApplication) getContext()).setLauncherProvider(this);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    synchronized public void loadDefaultFavoritesIfNecessary(int origWorkspaceResId){
        String spKey = LauncherApplication.getSharedPreferencesKey();
        SharedPreferences sp = getContext().getSharedPreferences(spKey, Context.MODE_PRIVATE);
        
        if (sp.getBoolean(DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED, false)) {
            int workspaceResId = origWorkspaceResId;
            
            // 如果没有给定ID则使用默认的
            if (workspaceResId == 0) {
                workspaceResId = sp.getInt(DEFAULT_WORKSPACE_RESOURCE_ID, R.xml.default_workspace);
            }
            
            // Populate favorites table with initial favorites
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(DB_CREATED_BUT_DEFAULT_WORKSPACE_NOT_LOADED);
            if (origWorkspaceResId != 0) {
                editor.putInt(DEFAULT_WORKSPACE_RESOURCE_ID, origWorkspaceResId);
            }
            sOpenHelper.loadFavorites(sOpenHelper.getWritableDatabase(), workspaceResId);
            sOpenHelper.updateSceneField(sOpenHelper.getWritableDatabase(), getContext().getString(R.string.scene_name_default));
            
        }
    }

    static class DatabaseHelper extends SQLiteOpenHelper{
        
        class FakedTypedArray{
            private AttributeSet mSet;
            private TypedArray mTypedArray;
            private String mValues[];
            
            boolean getBoolean(int i, boolean flag){
                if (mTypedArray == null) {
                    if ("true".equals(mValues[i])) {
                        flag = true;
                    }
                }else {
                    flag = mTypedArray.getBoolean(i, flag);
                }
                return flag;
            }
            
            int getInt(int i, int j){
                if (mTypedArray != null) {
                    j = mTypedArray.getInt(i, j);
                }else {
                    j = Integer.valueOf(mValues[i]).intValue();
                }
                return j;
            }
            
            String getString(int i){
                String string;
                if (mTypedArray == null) {
                    string = mValues[i];
                }else {
                    string = mTypedArray.getString(i);
                }
                return string;
            }
            
            void recycle(){
                if (mTypedArray != null)
                    mTypedArray.recycle();
            }
            
            public FakedTypedArray(Object object, int ai[]) {
                super();
                if (!(object instanceof XmlResourceParser)) {
                    mValues = new String[ai.length];
                    mSet = (AttributeSet)object;
                    for (int i = 0; i < mSet.getAttributeCount(); i++) {
                        String string = mSet.getAttributeName(i);
                        string = string.substring("launcher:".length(), string.length());
                        if (!"className".equals(string))
                        {
                            if (!"packageName".equals(string))
                            {
                                if (!"screen".equals(string))
                                {
                                    if (!"container".equals(string))
                                    {
                                        if (!"x".equals(string))
                                        {
                                            if (!"y".equals(string))
                                            {
                                                if (!"spanX".equals(string))
                                                {
                                                    if (!"spanY".equals(string))
                                                    {
                                                        if (!"icon".equals(string))
                                                        {
                                                            if (!"title".equals(string))
                                                            {
                                                                if (!"uri".equals(string))
                                                                {
                                                                    if (!"action".equals(string))
                                                                    {
                                                                        if (!"iconResource".equals(string))
                                                                        {
                                                                            if (!"retained".equals(string))
                                                                            {
                                                                                if ("presets_container".equals(string))
                                                                                    mValues[14] = mSet.getAttributeValue(i);
                                                                            } else
                                                                            {
                                                                                mValues[13] = mSet.getAttributeValue(i);
                                                                            }
                                                                        } else
                                                                        {
                                                                            mValues[12] = mSet.getAttributeValue(i);
                                                                        }
                                                                    } else
                                                                    {
                                                                        mValues[11] = mSet.getAttributeValue(i);
                                                                    }
                                                                } else
                                                                {
                                                                    mValues[10] = mSet.getAttributeValue(i);
                                                                }
                                                            } else
                                                            {
                                                                mValues[9] = mSet.getAttributeValue(i);
                                                            }
                                                        } else
                                                        {
                                                            mValues[8] = mSet.getAttributeValue(i);
                                                        }
                                                    } else
                                                    {
                                                        mValues[7] = mSet.getAttributeValue(i);
                                                    }
                                                } else
                                                {
                                                    mValues[6] = mSet.getAttributeValue(i);
                                                }
                                            } else
                                            {
                                                mValues[5] = mSet.getAttributeValue(i);
                                            }
                                        } else
                                        {
                                            mValues[4] = mSet.getAttributeValue(i);
                                        }
                                    } else
                                    {
                                        mValues[3] = mSet.getAttributeValue(i);
                                    }
                                } else
                                {
                                    mValues[2] = mSet.getAttributeValue(i);
                                }
                            } else
                            {
                                mValues[1] = mSet.getAttributeValue(i);
                            }
                        } else
                        {
                            mValues[0] = mSet.getAttributeValue(i);
                        }
                    }
                } else {
                    mTypedArray = mContext.obtainStyledAttributes((AttributeSet)object, ai);
                }
            }
        };
        
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite";
        private static final String TAG_CLOCK = "clock";
        private static final String TAG_SEARCH = "search";
        private static final String TAG_APPWIDGET = "appwidget";
        private static final String TAG_SHORTCUT = "shortcut";
        private static final String TAG_FOLDER = "folder";
        private static final String TAG_EXTRA = "extra";

        
        private final Context mContext;
        private final AppWidgetHost mAppWidgetHost;
        private long mMaxId = -1;
        
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            mAppWidgetHost = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
            
            if (mMaxId == -1) {
                mMaxId = initializeMaxId(getWritableDatabase());
            }
            
        }
        
        private void sendAppWidgetResetNotify() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(CONTENT_APPWIDGET_RESET_URI, null);
        }
        
        private int loadFavorites(SQLiteDatabase db){
            int i = 0;
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ContentValues contentValues = new ContentValues();
            PackageManager packageManager = mContext.getPackageManager();
            
            try {
                FileReader fileReader = new FileReader(ResConfig.getCustomizedDefaultWorkspaceXmlPath());
                
                XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
                if (xmlPullParser == null) {
                    xmlPullParser = mContext.getResources().getXml(ResConfig.getDefaultWorkspaceXmlId());
                }
                xmlPullParser.setInput(fileReader);
                AttributeSet attributeSet = Xml.asAttributeSet(xmlPullParser);
                XmlUtils.beginDocument(xmlPullParser, TAG_FAVORITES);
                int depth = xmlPullParser.getDepth();
                
                int type;
                while (((type = xmlPullParser.next()) != XmlPullParser.END_TAG ||
                        xmlPullParser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                    if (type != XmlPullParser.START_TAG) {
                        continue;
                    }
                    final String name = xmlPullParser.getName();
                    if (LOGD) {
                        Log.w(TAG, "MK : LoadFavorites: name = " + name);
                    }
                    
                    FakedTypedArray fakedTypedArray = new FakedTypedArray(attributeSet, R.styleable.Favorite);
                    
                    contentValues.clear();
                    
                    String screen = fakedTypedArray.getString(R.styleable.Favorite_screen);
                    
                }
                
            } catch (XmlPullParserException xmlPullParserException) {
                Log.w(TAG, "MK : Got exception xml parser. ", xmlPullParserException);
            } catch (IOException ioException) {
                Log.w(TAG, "MK : Got exception parsing favorites.", ioException);
            }
            
            return i;
        }
        
        private int loadPresetsApps(SQLiteDatabase db){
            int k = 0;
            return k;
        }
        
        private void createScreensTable(SQLiteDatabase db){
            
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mMaxId = 1L;
            db.execSQL("DROP TABLE IF EXISTS favorites");
            db.execSQL("CREATE TABLE favorites("
                    + "_id INTEGER PRIMARY KEY,"
                    + "title TEXT,"
                    + "intent TEXT,"
                    + "container INTEGER,"
                    + "screen INTEGER,"
                    + "cellX INTEGER,"
                    + "cellY INTEGER,"
                    + "spanX INTEGER,"
                    + "spanY INTEGER,"
                    + "itemType INTEGER,"
                    + "appWidgetId INTEGER NOT NULL DEFAULT -1,"
                    + "isShortcut INTEGER,"
                    + "iconType INTEGER,"
                    + "iconPackage TEXT,"
                    + "iconResource TEXT,"
                    + "icon BLOB,"
                    + "uri TEXT,"
                    + "displayMode INTEGER,"
                    + "launchCount INTEGER NOT NULL DEFAULT 1,"
                    + "sortMode INTEGER,"
                    + "itemFlags INTEGER NOT NULL DEFAULT 0"
                    + ");");
            
            if (mAppWidgetHost != null) {
                mAppWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }
            loadFavorites(db);
            loadPresetsApps(db);
            createScreensTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            
        }
        
        private long initializeMaxId(SQLiteDatabase db) {
            long id = -1;
            return id;
        }

        public int loadFavorites(SQLiteDatabase db, int workspaceResourceId) {
            int i = 0;
            return i;
        }
        
        public void updateSceneField(SQLiteDatabase db, String sceneName) {
            db.beginTransaction();
            try {
                db.execSQL("UPDATE favorites " + "SET scene = '" + sceneName
                        + "' WHERE scene IS NULL;");
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                // TODO: handle exception
            } finally{
                db.endTransaction();
            }
        }
    }

}
