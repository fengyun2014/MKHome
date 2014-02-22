package cn.minking.launcher;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;

public final class Utilities{
    private static final Paint sBlurPaint = new Paint();
    private static final Canvas sCanvas;
    static int sColorIndex = 0;
    static int sColors[];
    static float sDensity;
    static int sDensityDpi;
    private static final Paint sDisabledPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Rect sOldBounds = new Rect();
    private static Resources sSystemResource;
    
    static{
        sCanvas = new Canvas();
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(4, 2));
        int ai[] = new int[3];
        ai[0] = 0xffff0000;
        ai[1] = 0xff00ff00;
        ai[2] = 0xff0000ff;
        sColors = ai;
        sSystemResource = Resources.getSystem();
        sDensityDpi = sSystemResource.getDisplayMetrics().densityDpi;
        sDensity = sSystemResource.getDisplayMetrics().density;
    }
    public static int getDipPixelSize(int i){
        return (int)(0.5F + (float)i * sDensity);
    }
    
    
}