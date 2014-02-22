package cn.minking.launcher;

import java.io.ByteArrayOutputStream;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;

public class ItemInfo implements Cloneable {
    public int cellX;
    public int cellY;
    public long container;
    public long id;
    public boolean isGesture;
    public boolean isRetained;
    public int itemFlags;
    public int itemType;
    public int launchCount;
    public long screenId;
    public int spanX;
    public int spanY;

    public ItemInfo(){
        id = -1L;
        container = -1L;
        screenId = -1L;
        cellX = -1;
        cellY = -1;
        spanX = 1;
        spanY = 1;
        launchCount = 0;
        isGesture = false;
        isRetained = false;
    }
    
    public static byte[] flattenBitmap(Bitmap bitmap){
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, bytearrayoutputstream);
        return bytearrayoutputstream.toByteArray();
    }

    public static void writeBitmap(ContentValues contentvalues, Bitmap bitmap){
        if (bitmap != null)
            contentvalues.put("icon", flattenBitmap(bitmap));
    }

    @Override
    public Object clone(){
        ItemInfo iteminfo;
        try
        {
            iteminfo = (ItemInfo)super.clone();
        }
        catch (CloneNotSupportedException _ex)
        {
            throw new AssertionError();
        }
        return iteminfo;
    }
    
    public void copyPosition(ItemInfo iteminfo){
        container = iteminfo.container;
        screenId = iteminfo.screenId;
        cellX = iteminfo.cellX;
        cellY = iteminfo.cellY;
    }

    public boolean isCustomizedIcon(){
        boolean flag;
        if (!isRetained && (2 & itemFlags) == 0)
            flag = false;
        else
            flag = true;
        return flag;
    }

    public boolean isPresetApp(){
        boolean flag;
        if ((1 & itemFlags) == 0)
            flag = false;
        else
            flag = true;
        return flag;
    }
    
    public void load(Cursor cursor){
        long l = 0;
        int i;
        id = cursor.getLong(0);
        
        if (!cursor.isNull(11))
            i = cursor.getInt(11);
        else
            i = 0;
        
        cellX = i;
        if (!cursor.isNull(12))
            l = cursor.getInt(12);
        cellY = (int)l;
        spanX = cursor.getInt(13);
        spanY = cursor.getInt(14);
        
        if (!cursor.isNull(10))
            l = cursor.getLong(10);
        else
            l = 0L;
        
        screenId = l;
        itemType = cursor.getInt(8);
        container = cursor.getLong(7);
        launchCount = cursor.getInt(17);
        itemFlags = cursor.getInt(19);
    }    
    
    public void loadPosition(ContentValues contentvalues){
        container = contentvalues.getAsLong("container").longValue();
        screenId = contentvalues.getAsLong("screen").longValue();
        cellX = contentvalues.getAsInteger("cellX").intValue();
        cellY = contentvalues.getAsInteger("cellY").intValue();
    }
    
    public void onAddToDatabase(ContentValues contentvalues){
        contentvalues.put("itemType", Integer.valueOf(itemType));
        if (!isGesture) {
            contentvalues.put("container", Long.valueOf(container));
            contentvalues.put("screen", Long.valueOf(screenId));
            contentvalues.put("cellX", Integer.valueOf(cellX));
            contentvalues.put("cellY", Integer.valueOf(cellY));
            contentvalues.put("spanX", Integer.valueOf(spanX));
            contentvalues.put("spanY", Integer.valueOf(spanY));
            contentvalues.put("launchCount", Integer.valueOf(launchCount));
            contentvalues.put("itemFlags", Integer.valueOf(itemFlags));
        }
    }
    
    public void onLaunch(){
        launchCount = 1 + launchCount;
    }

    public String toString(){
        return (new StringBuilder()).append("Item(id=")
                .append(id).append(" type=").append(itemType).append(")").toString();
    }

    public void unbind(){
    }
}
