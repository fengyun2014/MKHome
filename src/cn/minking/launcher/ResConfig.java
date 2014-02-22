package cn.minking.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class ResConfig {
    private static int mCellCountX = -1;
    private static int mCellCountY = -1;
    private static String mCustomizedDefaultWorkspacePath;
    private static int mDefaultWorkspaceId;
    private static String mDefaultWorkspaceName;
    private static int mHotseatCount = -1;
    private static int mIconHeight = -1;
    private static int mIconWidth = -1;
    private static String mLauncherDatabaseName;
    private static int mWidgetCellMeasureHeight;
    private static int mWidgetCellMeasureWidth;
    private static int mWidgetCellMinHeight;
    private static int mWidgetCellMinWidth;

    public static void Init(Context context) {
        int i = 0;
        Resources resources = context.getResources();
        mIconWidth = resources.getDimensionPixelSize(R.dimen.config_icon_width);
        mIconHeight = resources.getDimensionPixelSize(R.dimen.config_icon_height);
        mCellCountX = Math.max(2, resources.getInteger(R.integer.config_cell_count_x));
        mCellCountY = Math.max(2, resources.getInteger(R.integer.config_cell_count_y));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        if (mCellCountX != 3 || mCellCountY != 3)
        {
            String s = sharedPreferences.getString("pref_key_cell_layout_size", null);
            if (s != null)
            {
                int index = s.indexOf('x');
                if (index != -1)
                {
                    mCellCountX = Integer.parseInt(s.substring(0, i));
                    mCellCountY = Integer.parseInt(s.substring(i + 1, s.length()));
                    if (mCellCountX < 2 || mCellCountY < 2)
                    {
                        mCellCountY = 4;
                        mCellCountX = 4;
                    }
                }
            }
        }
        mHotseatCount = mCellCountX + mCellCountX / 2;
        String cellSize = getCellSizeVal(mCellCountX, mCellCountY);
        mLauncherDatabaseName = getDatabaseNameBySuffix(cellSize);
        StringBuilder stringbuilder = (new StringBuilder()).append("default_workspace");
        
        if ("4x4".equals(cellSize)){
            cellSize = "";
        }
            
        mDefaultWorkspaceName = stringbuilder.append(i).toString();
        mCustomizedDefaultWorkspacePath = (new StringBuilder()).append("/data/media/customized/").append(mDefaultWorkspaceName).append(".xml").toString();
        mDefaultWorkspaceName = (new StringBuilder()).append(context.getPackageName()).append(":xml/").append(mDefaultWorkspaceName).toString();
        mDefaultWorkspaceId = resources.getIdentifier(mDefaultWorkspaceName, null, null);
        if (mDefaultWorkspaceId == 0)
            mDefaultWorkspaceId = R.xml.default_workspace_none;
        mWidgetCellMeasureWidth = resources.getDimensionPixelSize(R.dimen.workspace_widget_cell_measure_width);
        mWidgetCellMeasureHeight = resources.getDimensionPixelSize(R.dimen.workspace_widget_cell_measure_height);
        mWidgetCellMinWidth = resources.getDimensionPixelSize(R.dimen.workspace_widget_cell_min_width);
        mWidgetCellMinHeight = resources.getDimensionPixelSize(R.dimen.workspace_widget_cell_min_height);
    }
    
    public static final void calcWidgetSpans(LauncherAppWidgetProviderInfo launcherappwidgetproviderinfo){
        launcherappwidgetproviderinfo.spanX = getWidgetSpanX(launcherappwidgetproviderinfo.providerInfo.minWidth);
        launcherappwidgetproviderinfo.spanY = getWidgetSpanY(launcherappwidgetproviderinfo.providerInfo.minHeight);
    }

    public static final int getCellCountX(){
        return mCellCountX;
    }

    public static final int getCellCountY(){
        return mCellCountY;
    }

    public static final String getCellSizeVal(int i, int j){
        return (new StringBuilder()).append(i).append("x").append(j).toString();
    }

    public static final String getCustomizedDefaultWorkspaceXmlPath(){
        return mCustomizedDefaultWorkspacePath;
    }

    public static final String getDatabaseName(){
        return mLauncherDatabaseName;
    }

    public static final String getDatabaseNameBySuffix(String s){
        if ("4x4".equals(s))
            s = "";
        return (new StringBuilder()).append("launcher").append(s).append(".db").toString();
    }

    public static final int getDefaultWorkspaceXmlId(){
        return mDefaultWorkspaceId;
    }

    public static final int getHotseatCount(){
        return mHotseatCount;
    }

    public static final int getIconHeight(){
        return mIconHeight;
    }

    public static final int getIconWidth(){
        return mIconWidth;
    }

    public static final int getWidgetCellMinHeight(){
        return mWidgetCellMinHeight;
    }

    public static final int getWidgetCellMinWidth(){
        return mWidgetCellMinWidth;
    }

    public static final int getWidgetSpanX(int i){
        return Math.min(1 + (i + Utilities.getDipPixelSize(1)) / mWidgetCellMeasureWidth, mCellCountX);
    }

    public static final int getWidgetSpanY(int i){
        return Math.min(1 + (i + Utilities.getDipPixelSize(1)) / mWidgetCellMeasureHeight, mCellCountY);
    }
}
