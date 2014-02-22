package cn.minking.launcher;

import android.appwidget.AppWidgetProviderInfo;

class LauncherAppWidgetProviderInfo extends ItemInfo{
    AppWidgetProviderInfo providerInfo;

    LauncherAppWidgetProviderInfo(AppWidgetProviderInfo appwidgetproviderinfo)
    {
        itemType = 6;
        providerInfo = appwidgetproviderinfo;
    }

    public LauncherAppWidgetProviderInfo clone()
    {
        return (LauncherAppWidgetProviderInfo)super.clone();
    }

}