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

package com.gsma.mobileconnect.utils;

import org.apache.http.NameValuePair;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HttpUtilsTest
{
    @Test
    public void getCookiesToProxy_withCookiesEnabled_shouldFilterCookies()
    {
        // GIVEN
        List<KeyValuePair> currentCookies = new ArrayList<KeyValuePair>(5);
        List<KeyValuePair> expectedCookies = new ArrayList<KeyValuePair>(3);

        KeyValuePair kvp = new KeyValuePair("key1", "value");
        currentCookies.add(kvp);
        kvp = new KeyValuePair( "Most-Recent-Selected-Operator-Expiry", "value");
        currentCookies.add(kvp);
        expectedCookies.add(kvp);
        kvp = new KeyValuePair( "Most-Recent-Selected-Operator", "value");
        currentCookies.add(kvp);
        expectedCookies.add(kvp);
        kvp = new KeyValuePair( "Enum-Nonce", "value");
        currentCookies.add(kvp);
        expectedCookies.add(kvp);
        kvp = new KeyValuePair("key2", "value");
        currentCookies.add(kvp);

        // WHEN
        List<KeyValuePair> cookiesToProxy = HttpUtils.getCookiesToProxy(true, currentCookies);

        // THEN
        assertEquals(expectedCookies, cookiesToProxy);
    }

    @Test
    public void getCookiesToProxy_withoutCookiesEnable_shouldReturnAllCookies()
    {
        // GIVEN
        List<KeyValuePair> currentCookies = new ArrayList<KeyValuePair>(5);
        List<KeyValuePair> expectedCookies = new ArrayList<KeyValuePair>(3);

        KeyValuePair kvp = new KeyValuePair("key1", "value");
        currentCookies.add(kvp);
        kvp = new KeyValuePair( "Most-Recent-Selected-Operator-Expiry", "value");
        currentCookies.add(kvp);
        kvp = new KeyValuePair( "Most-Recent-Selected-Operator", "value");
        currentCookies.add(kvp);
        kvp = new KeyValuePair( "Enum-Nonce", "value");
        currentCookies.add(kvp);
        kvp = new KeyValuePair("key2", "value");
        currentCookies.add(kvp);

        // WHEN
        List<KeyValuePair> cookiesToProxy = HttpUtils.getCookiesToProxy(false, currentCookies);

        // THEN
        assertEquals(expectedCookies, cookiesToProxy);
    }

    @Test
    public void extractParameters_withEmptyURL_shouldExtractNoParameters()
            throws URISyntaxException
    {
        // GIVEN
        String url = "";

        // WHEN
        List<NameValuePair> parameters = HttpUtils.extractParameters(url);

        // THEN
        assertTrue(parameters.isEmpty());
    }
}
