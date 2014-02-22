package cn.minking.launcher;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

public class LauncherSettings{
    public static final class Packages implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse("content://cn.minking.launcher.settings/packages");
    }
    
    public static final class Screens implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.parse("content://cn.minking.launcher.settings/screens");
    }
    
    public static final class Favorites implements BaseLauncherColumns{
        public static final Uri CONTENT_URI = 
                Uri.parse("content://cn.minking.launcher.settings/favorites");
        
        public static Uri getContentUri(long l) {
            return Uri.parse((new StringBuilder()).
                    append("content://cn.minking.launcher.settings/favorites/").append(l).toString());
        }
        
        public static Uri getJoinContentUri(String s) {
            return Uri.parse((new StringBuilder()).
                    append("content://cn.minking.launcher.settings/favorites").append(s).toString());
        }
        
    }
    
    public static void deletePackage(Context context, String string){
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", string);
        contentValues.put("delete", Boolean.valueOf(true));
        contentResolver.update(Packages.CONTENT_URI, contentValues, null, null);
    }
    
    public static boolean isRetainedComponent(ComponentName componentName){
        return "com.android.stk".equals(componentName.getPackageName());
    }
    
    public static void updateHomeScreen(Context context, String string) {
        updateHomeScreen(context, string, false);
    }
    
    public static void updateHomeScreen(Context context, String string, boolean flag) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", string);
        contentValues.put("keepItem", Boolean.valueOf(flag));
        contentResolver.update(Packages.CONTENT_URI, contentValues, null, null);
    }
    
    public static interface BaseLauncherColumns extends BaseColumns{
        
    }
    
    
}