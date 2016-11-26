package com.gsma.mobileconnect.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

/**
 * A simple {@link Dialog} which simply expands to the size of the parent window.
 * <p>
 * A singleton instance is provided which should be used when using
 * {@link com.gsma.mobileconnect.helpers.DiscoveryService} and {@link com.gsma.mobileconnect.helpers.AuthorizationService} as they both require a {@link Dialog} for the same purpose.
 * Created by Usmaan.Dad on 6/17/2016.
 */
public class DiscoveryAuthenticationDialog extends Dialog
{
    private WindowManager.LayoutParams layoutParams;

    private static DiscoveryAuthenticationDialog instance;

//    /**
    //     * When discovering and authentic
    //     *
    //     * @param context
    //     * @return
    //     */
    //    public static DiscoveryAuthenticationDialog getInstance(Context context)
    //    {
    //        if (instance == null && context != null)
    //        {
    //            instance = new DiscoveryAuthenticationDialog(context);
    //        }
    //        return instance;
    //    }

    public DiscoveryAuthenticationDialog(final Context context)
    {
        super(context);
        initialise();
    }

    /**
     * Initialises the current instance of the {@link Dialog} such that it's made to match the size of the parent
     * window.
     */
    private void initialise()
    {
        setCancelable(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        setOnShowListener(new OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                getWindow().setAttributes(layoutParams);
            }
        });
    }
}