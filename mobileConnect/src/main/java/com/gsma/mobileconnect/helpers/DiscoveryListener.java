package com.gsma.mobileconnect.helpers;

/**
 * A Listener which exposes Success and Failure callbacks whilst attempting to perform Discovery.
 * <p/>
 * Created by Usmaan.Dad on 6/21/2016.
 */
public interface DiscoveryListener
{
    /**
     * A successful discovery has been completed and has returned a populated
     * {@link MobileConnectStatus}.
     *
     * @param mobileConnectStatus The result of the discovery.
     */
    void discoveryComplete(MobileConnectStatus mobileConnectStatus);

    /**
     * The discovery has failed to succeed. A a populated
     * {@link MobileConnectStatus} with the error
     *
     * @param mobileConnectStatus The {@link MobileConnectStatus} containing the error
     */
    void discoveryFailed(MobileConnectStatus mobileConnectStatus);

    /**
     * This is called when the discovery dialog has been dismissed.
     */
    void onDiscoveryDialogClose();
}