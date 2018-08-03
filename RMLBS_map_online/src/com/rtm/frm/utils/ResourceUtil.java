package com.rtm.frm.utils;

import android.content.Context;

public class ResourceUtil {
	public static int getLayoutId(Context paramContext, String paramString) { 
        return paramContext.getResources().getIdentifier(paramString, "layout", 
                paramContext.getPackageName()); 
    } 
 
    public static int getStringId(Context paramContext, String paramString) { 
        return paramContext.getResources().getIdentifier(paramString, "string", 
                paramContext.getPackageName()); 
    } 
 
    public static int getDrawableId(Context paramContext, String paramString) { 
        return paramContext.getResources().getIdentifier(paramString, 
                "drawable", paramContext.getPackageName()); 
    } 
     
}
