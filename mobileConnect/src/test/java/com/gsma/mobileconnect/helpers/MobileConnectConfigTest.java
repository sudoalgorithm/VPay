/*
 *                                   SOFTWARE USE PERMISSION
 *
 *  By downloading and accessing this software and associated documentation files ("Software") you are granted the
 *  unrestricted right to deal in the Software, including, without limitation the right to use, copy, modify, publish,
 *  sublicense and grant such rights to third parties, subject to the following conditions:
 *
 *  The following copyright notice and this permission notice shall be included in all copies, modifications or
 *  substantial portions of this Software: Copyright © 2016 GSM Association.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS," WITHOUT WARRANTY OF ANY KIND, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. YOU
 *  AGREE TO INDEMNIFY AND HOLD HARMLESS THE AUTHORS AND COPYRIGHT HOLDERS FROM AND AGAINST ANY SUCH LIABILITY.
 */

package com.gsma.mobileconnect.helpers;

import com.gsma.mobileconnect.discovery.CompleteSelectedOperatorDiscoveryOptions;
import com.gsma.mobileconnect.discovery.DiscoveryOptions;
import com.gsma.mobileconnect.discovery.IPreferences;
import com.gsma.mobileconnect.oidc.AuthenticationOptions;
import com.gsma.mobileconnect.oidc.TokenOptions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MobileConnectConfigTest
{
    @Test
    public void getDiscoveryOptions_allValuesSetInConfig_allValuesShouldBePopulated()
    {
        // GIVEN
        String localClientIP = "10.0.0.1";
        String specifiedClientIP = "127.0.0.1";
        String identifiedClientIP = "127.0.0.2";
        String identifiedMCC = "901";
        String identifiedMNC = "01";
        int timeout = 40;
        boolean manuallySelect = true;
        boolean cookiesEnabled = false;
        boolean usingMobileData = true;
        boolean shouldClientIPBeAddedToDiscoveryRequest = true;

        MobileConnectConfig config = new MobileConnectConfig();
        config.setLocalClientIP(localClientIP);
        config.setClientIP(specifiedClientIP);
        config.setIdentifiedMCC(identifiedMCC);
        config.setIdentifiedMNC(identifiedMNC);
        config.setNetworkTimeout(timeout);
        config.setManuallySelect(manuallySelect);
        config.setCookiesEnabled(cookiesEnabled);
        config.setUsingMobileData(usingMobileData);
        config.setShouldClientIPBeAddedToDiscoveryRequest(shouldClientIPBeAddedToDiscoveryRequest);

        // WHEN
        DiscoveryOptions discoveryOptions = config.getDiscoveryOptions(identifiedClientIP);

        //THEN
        assertEquals(localClientIP, discoveryOptions.getLocalClientIP());
        assertEquals(specifiedClientIP, discoveryOptions.getClientIP());
        assertEquals(identifiedMCC, discoveryOptions.getIdentifiedMCC());
        assertEquals(identifiedMNC, discoveryOptions.getIdentifiedMNC());
        assertEquals(timeout, discoveryOptions.getTimeout());
        assertEquals(manuallySelect, discoveryOptions.isManuallySelect());
        assertEquals(cookiesEnabled, discoveryOptions.isCookiesEnabled());
        assertEquals(usingMobileData, discoveryOptions.isUsingMobileData());
    }

    @Test
    public void getDiscoveryOptions_noValuesSetInConfig_onlyDefaultValuesSet()
    {
        // GIVEN
        String identifiedClientIP = "127.0.0.2";
        int defaultTimeout = 30000;

        MobileConnectConfig config = new MobileConnectConfig();
        // WHEN
        DiscoveryOptions discoveryOptions = config.getDiscoveryOptions(identifiedClientIP);

        //THEN
        assertNull(discoveryOptions.getLocalClientIP());
        assertNull(discoveryOptions.getClientIP());
        assertNull(discoveryOptions.getIdentifiedMCC());
        assertNull(discoveryOptions.getIdentifiedMNC());
        assertEquals(defaultTimeout, discoveryOptions.getTimeout());
        assertFalse(discoveryOptions.isManuallySelect());
        assertTrue(discoveryOptions.isCookiesEnabled());
        assertNull(discoveryOptions.isUsingMobileData());
    }

    @Test
    public void getDiscoveryOptions_setClientIPShouldBeAddedToDiscoveryRequest_shouldAddIdentifiedClientIP()
    {
        // GIVEN
        String identifiedClientIP = "127.0.0.2";
        boolean shouldClientIPBeAddedToDiscoveryRequest = true;

        MobileConnectConfig config = new MobileConnectConfig();
        config.setShouldClientIPBeAddedToDiscoveryRequest(shouldClientIPBeAddedToDiscoveryRequest);

        // WHEN
        DiscoveryOptions discoveryOptions = config.getDiscoveryOptions(identifiedClientIP);

        //THEN
        assertEquals(identifiedClientIP, discoveryOptions.getClientIP());
    }

    @Test
    public void shouldProvideIPreferences()
    {
        // GIVEN
        String clientId = "CLIENT_ID";
        String clientSecret = "CLIENT_SECRET";
        String discoveryURL = "DISCOVERY_URL";

        MobileConnectConfig config = new MobileConnectConfig();
        config.setClientId(clientId);
        config.setClientSecret(clientSecret);
        config.setDiscoveryURL(discoveryURL);

        // WHEN
        IPreferences preferences = config;

        // THEN
        assertEquals(clientId, preferences.getClientId());
        assertEquals(clientSecret, preferences.getClientSecret());
        assertEquals(discoveryURL, preferences.getDiscoveryURL());
    }

    @Test
    public void getCompleteSelectedOperatorDiscoveryOptions_allValuesSetInConfig_allValuesShouldBePopulated()
    {
        // GIVEN
        boolean cookiesEnabled = false;
        int timeout = 50;
        MobileConnectConfig config = new MobileConnectConfig();
        config.setCookiesEnabled(cookiesEnabled);
        config.setNetworkTimeout(timeout);

        // WHEN
        CompleteSelectedOperatorDiscoveryOptions options = config.getCompleteSelectedOperatorDiscoveryOptions();

        // THEN
        assertEquals(cookiesEnabled, options.isCookiesEnabled());
        assertEquals(timeout, options.getTimeout());
    }

    @Test
    public void getCompleteSelectedOperatorDiscoveryOptions_noValuesSetInConfig_onlyDefaultValuesSet()
    {
        // GIVEN
        int defaultTimeout = 30000;
        MobileConnectConfig config = new MobileConnectConfig();

        // WHEN
        CompleteSelectedOperatorDiscoveryOptions options = config.getCompleteSelectedOperatorDiscoveryOptions();

        // THEN
        assertTrue(options.isCookiesEnabled());
        assertEquals(defaultTimeout, options.getTimeout());
    }

    @Test
    public void getAuthenticationOptions_withAllValuesSetInConfig_allValuesShouldBePopulated()
    {
        // GIVEN
        int timeout = 50;
        String claimsLocales = "CLAIMS-LOCALES";
        String display = "DISPLAY";
        String dtbs = "DTBS";
        String idTokenHint = "ID-TOKEN-HINT";
        String loginHint = "LOGIN-HINT";
        String prompt = "PROMPT";
        String screenMode = "SCREEN-MODE";
        String uiLocales = "UI-LOCALES";
        MobileConnectConfig config = new MobileConnectConfig();
        config.setAuthorizationTimeout(timeout);
        config.setClaimsLocales(claimsLocales);
        config.setDisplay(display);
        config.setDtbs(dtbs);
        config.setIdTokenHint(idTokenHint);
        config.setLoginHint(loginHint);
        config.setPrompt(prompt);
        config.setScreenMode(screenMode);
        config.setUiLocales(uiLocales);

        // WHEN
        AuthenticationOptions options = config.getAuthenticationOptions();

        // THEN
        assertEquals(timeout, options.getTimeout());
        assertEquals(claimsLocales, options.getClaimsLocales());
        assertEquals(display, options.getDisplay());
        assertEquals(dtbs, options.getDtbs());
        assertEquals(idTokenHint, options.getIdTokenHint());
        assertEquals(loginHint, options.getLoginHint());
        assertEquals(prompt, options.getPrompt());
        assertEquals(screenMode, options.getScreenMode());
        assertEquals(uiLocales, options.getUiLocales());
    }

    @Test
    public void getAuthenticationOptions_withNoValuesSetInConfig_onlyDefaultValuesSet()
    {
        // GIVEN
        int defaultTimeout = 300000;
        String defaultDisplay = "page";
        String defaultScreenMode = "overlay";
        MobileConnectConfig config = new MobileConnectConfig();

        // WHEN
        AuthenticationOptions options = config.getAuthenticationOptions();

        // THEN
        assertEquals(defaultTimeout, options.getTimeout());
        assertNull(options.getClaimsLocales());
        assertEquals(defaultDisplay, options.getDisplay());
        assertNull(options.getDtbs());
        assertNull(options.getIdTokenHint());
        assertNull(options.getLoginHint());
        assertNull(options.getPrompt());
        assertEquals(defaultScreenMode, options.getScreenMode());
        assertNull(options.getUiLocales());
    }

    @Test
    public void getTokenOptions_withAllValuesSetInConfig_allValuesShouldBePopulated()
    {
        // GIVEN
        int timeout = 50;
        boolean idTokenValidationRequired = false;
        MobileConnectConfig config = new MobileConnectConfig();
        config.setNetworkTimeout(timeout);
        config.setIdTokenValidationRequired(idTokenValidationRequired);

        // WHEN
        TokenOptions options = config.getTokenOptions();

        // THEN
        assertEquals(timeout, options.getTimeout());
        assertEquals(idTokenValidationRequired, options.isCheckIdTokenSignature());
    }

    @Test
    public void getTokenOptions_withNoValuesSetInConfig_onlyDefaultValuesSet()
    {
        // GIVEN
        int defaultTimeout = 30000;
        boolean defaultCheckIdTokenSignature = true;
        MobileConnectConfig config = new MobileConnectConfig();

        // WHEN
        TokenOptions options = config.getTokenOptions();

        // THEN
        assertEquals(defaultTimeout, options.getTimeout());
        assertEquals(defaultCheckIdTokenSignature, options.isCheckIdTokenSignature());
    }

}
