package com.gsma.mobileconnect.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by Usmaan.Dad on 6/17/2016.
 */
public class InteractableWebView extends WebView
{
    public InteractableWebView(Context context)
    {
        super(context);
        initialise(context);
    }

    public InteractableWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initialise(context);
    }

    public InteractableWebView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initialise(context);
    }

    private void initialise(Context context)
    {
        if (!isInEditMode())
        {
            if (Build.VERSION.SDK_INT >= 19)
            {
                setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            else
            {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus(View.FOCUS_DOWN);

            WebSettings settings = getSettings();

            settings.setJavaScriptEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            settings.setSupportMultipleWindows(false);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setSupportZoom(false);
            settings.setUseWideViewPort(false);

            String databasePath = context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
            settings.setDatabasePath(databasePath);
        }
    }
}