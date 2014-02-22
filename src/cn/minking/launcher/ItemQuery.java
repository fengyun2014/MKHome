package cn.minking.launcher;


interface ItemQuery{
    String as[] = new String[]{ 
            "favorites._id",
            "intent",
            "favorites.title",
            "iconType",
            "icon",
            "iconPackage",
            "iconResource",
            "container",
            "itemType",
            "appWidgetId",
            "screen",
            "cellX",
            "cellY",
            "spanX",
            "spanY",
            "uri",
            "displayMode",
            "launchCount",
            "sortMode",
            "itemFlags"};
            
    public static final String COLUMNS[] = as;
}