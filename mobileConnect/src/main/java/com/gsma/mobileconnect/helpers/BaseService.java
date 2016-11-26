package com.gsma.mobileconnect.helpers;

import com.gsma.mobileconnect.utils.MobileConnectState;

import java.util.UUID;

/**
 * Base Class for the Services.
 * Created by nick.copley on 03/03/2016.
 */
public class BaseService
{

    private static final int HTTP_OK = 200;

    private static final int HTTP_ACCEPTED = 202;

    /**
     * Generate a unique string with the optional prefix.
     *
     * @param prefix Optional prefix for the generated string.
     * @return A unique string.
     */
    public String generateUniqueString(String prefix)
    {
        if (null == prefix)
        {
            prefix = "";
        }
        return prefix + UUID.randomUUID().toString();
    }

    /**
     * Check if the response code indicates a successful response.
     *
     * @param responseCode The response code to check.
     * @return True if the request was successful.
     */
    boolean isSuccessResponseCode(int responseCode)
    {
        return HTTP_OK == responseCode || HTTP_ACCEPTED == responseCode;
    }

    /**
     * Test whether the mobileConnectState has a discovery response.
     *
     * @param mobileConnectState The mobile connect state to check.
     * @return True if there is a discovery response present.
     */
    boolean hasDiscoveryResponse(MobileConnectState mobileConnectState)
    {
        return (null != mobileConnectState && null != mobileConnectState.getDiscoveryResponse());
    }
}
