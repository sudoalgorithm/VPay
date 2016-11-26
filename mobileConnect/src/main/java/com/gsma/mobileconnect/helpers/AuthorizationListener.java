package com.gsma.mobileconnect.helpers;

import com.gsma.mobileconnect.oidc.RequestTokenResponse;

/**
 * This Listener handles the tokens being returned from the Authorization service.
 * Created by nick.copley on 23/02/2016.
 */
public interface AuthorizationListener
{
    /**
     * A RequestTokenResponse has been received. Please note that this doesn't mean the token is valid
     * for example it could have expired.
     *
     * @param response a token response
     */
    void tokenReceived(RequestTokenResponse response);

    /**
     * In the event an Authorization failed this method will be called by the SDK. It will return a status object that
     * will contain further details of the error or a notification that discovery is required.
     *
     * @param mobileConnectStatus A populated {@link MobileConnectStatus} containing the errors.
     */
    void authorizationFailed(MobileConnectStatus mobileConnectStatus);

    /**
     * This is called when the authorization dialog has been dismissed.
     */
    void onAuthorizationDialogClose();
}