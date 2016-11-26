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
import com.gsma.mobileconnect.discovery.DiscoveryException;
import com.gsma.mobileconnect.discovery.DiscoveryOptions;
import com.gsma.mobileconnect.discovery.DiscoveryResponse;
import com.gsma.mobileconnect.discovery.IDiscovery;
import com.gsma.mobileconnect.discovery.IDiscoveryResponseCallback;
import com.gsma.mobileconnect.discovery.IParsedDiscoveryRedirectCallback;
import com.gsma.mobileconnect.discovery.IPreferences;
import com.gsma.mobileconnect.discovery.ParsedDiscoveryRedirect;
import com.gsma.mobileconnect.model.DiscoveryModel;
import com.gsma.mobileconnect.oidc.AuthenticationOptions;
import com.gsma.mobileconnect.oidc.DiscoveryResponseExpiredException;
import com.gsma.mobileconnect.oidc.IOIDC;
import com.gsma.mobileconnect.oidc.IParseAuthenticationResponseCallback;
import com.gsma.mobileconnect.oidc.IRequestTokenCallback;
import com.gsma.mobileconnect.oidc.IStartAuthenticationCallback;
import com.gsma.mobileconnect.oidc.OIDCException;
import com.gsma.mobileconnect.oidc.ParsedAuthorizationResponse;
import com.gsma.mobileconnect.oidc.ParsedIdToken;
import com.gsma.mobileconnect.oidc.RequestTokenResponse;
import com.gsma.mobileconnect.oidc.RequestTokenResponseData;
import com.gsma.mobileconnect.oidc.StartAuthenticationResponse;
import com.gsma.mobileconnect.oidc.TokenOptions;
import com.gsma.mobileconnect.utils.ErrorResponse;
import com.gsma.mobileconnect.utils.KeyValuePair;
import com.gsma.mobileconnect.utils.MobileConnectState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AuthorizationServiceTest
{
    MobileConnectConfig config;

    @Before
    public void before()
    {
        AuthorizationService connect = new AuthorizationService();
        DiscoveryModel.getInstance().setDiscoveryServiceRedirectedURL("url");
        config = new MobileConnectConfig();
        // Registered application client id
        config.setClientId("7854b859-375d-47b9-a9c9-dce2647f1b76");

        // Registered application client secret
        config.setClientSecret("cb70b357-8203-4f3f-824c-f9d4990bee99");

        // Registered application url
        config.setApplicationURL("http://localhost:8080/mobile_connect");

        // URL of the Mobile Connect Discovery End Point
        config.setDiscoveryURL("http://discovery.sandbox2.mobileconnect.io/v2/discovery");

        // URL to inform the Discovery End Point to redirect to, this should route to the "/discovery_redirect"
        // handler below
        config.setDiscoveryRedirectURL("http://localhost:8080/mobileconnect/discovery_redirect");

        // Authorization State would typically set to a unique value
        config.setAuthorizationState(connect.generateUniqueString("state_"));

        // Authorization Nonce would typically set to a unique value
        config.setAuthorizationNonce(connect.generateUniqueString("nonce_"));

    }

    @Test
    public void callMobileConnectForStartDiscovery_whenDiscoveryThrowsException_returnedStatusShouldBeError() throws
                                                                                                              DiscoveryException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);
        DiscoveryService service = new DiscoveryService();
        DiscoveryException expectedException = new DiscoveryException("Test");
        mockStartAutomatedOperatorDiscoveryThrowException(mockedDiscovery, expectedException);
        service.setDiscovery(mockedDiscovery);
        // WHEN
        MobileConnectStatus status = service.callMobileConnectForStartDiscovery(config);

        // THEN
        assertTrue(status.isError());
        assertEquals(expectedException, status.getException());
        assertEquals("internal error", status.getError());
        assertEquals("Failed to obtain operator details.", status.getDescription());
    }

    @Test
    public void callMobileConnectForStartDiscovery_whenDiscoveryResultsInError_returnedStatusShouldBeError() throws
                                                                                                             DiscoveryException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();
        // return error from startAutomatedOperatorDiscovery
        int responseCode = 404;
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, responseCode, null, null);
        mockStartAutomatedOperatorDiscoverySuccess(mockedDiscovery, expectedDiscoveryResponse);

        // set up return of getErrorResponse
        String expectedError = "ERROR";
        String expectedErrorDescription = "ERROR-DESCRIPTION";
        mockGetErrorResponse(mockedDiscovery, expectedDiscoveryResponse, expectedError, expectedErrorDescription);
        connect.setDiscovery(mockedDiscovery);
        // WHEN
        MobileConnectStatus status = connect.callMobileConnectForStartDiscovery(config);

        // THEN
        assertTrue(status.isError());
        assertEquals(expectedError, status.getError());
        assertEquals(expectedErrorDescription, status.getDescription());
        assertEquals(expectedDiscoveryResponse, status.getDiscoveryResponse());
    }

    @Test
    public void
    callMobileConnectForStartDiscovery_whenDiscoveryResultsInOperatorNotIdentified_returnedStatusShouldBeOperatorSelection() throws
                                                                                                                                         DiscoveryException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();

        // success discovery response
        DiscoveryResponse discoveryResponse = new DiscoveryResponse(false, null, 202, null, null);
        mockStartAutomatedOperatorDiscoverySuccess(mockedDiscovery, discoveryResponse);

        // set up return of extractOperatorSelectionURL
        String expectedUrl = "EXPECTED-URL";
        mockExtractOperatorSelectionURL(mockedDiscovery, expectedUrl);

        connect.setDiscovery(mockedDiscovery);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectForStartDiscovery(config);

        // THEN
        assertTrue(status.isOperatorSelection());
        assertEquals(expectedUrl, status.getUrl());
    }

    @Test
    public void
    callMobileConnectForStartDiscovery_whenDiscoveryResultsInAuthorization_returnedStatusShouldBeAuthorization() throws
                                                                                                                             DiscoveryException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();
        connect.setDiscovery(mockedDiscovery);
        // success discovery response
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        mockStartAutomatedOperatorDiscoverySuccess(mockedDiscovery, expectedDiscoveryResponse);

        // set up return of extractOperatorSelectionURL
        mockExtractOperatorSelectionURL(mockedDiscovery, "");

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectForStartDiscovery(config);

        // THEN
        assertTrue(status.isStartAuthorization());
        assertEquals(expectedDiscoveryResponse, status.getDiscoveryResponse());
    }

    @Test
    public void callMobileConnectOnDiscoveryRedirect_whenDiscoveryThrowsURIException_returnedStatusShouldBeError()
            throws
                                                                                                                   DiscoveryException,
                                                                                                                   URISyntaxException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);


        DiscoveryService connect = new DiscoveryService();
        // set up exception
        URISyntaxException expectedException = new URISyntaxException("URI", "URI");
        mockParseDiscoveryRedirectThrowsException(mockedDiscovery, expectedException);
        connect.setDiscovery(mockedDiscovery);
        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnDiscoveryRedirect(config);

        // THEN
        assertTrue(status.isError());
        assertEquals(expectedException, status.getException());
        assertEquals("internal error", status.getError());
        assertEquals("Cannot parse the redirect parameters.", status.getDescription());
    }

    @Test
    public void
    callMobileConnectOnDiscoveryRedirect_whenRedirectHasNoOperatorDetails_returnedStatusShouldBeStartDiscovery() throws
                                                                                                                             DiscoveryException,
                                                                                                                             URISyntaxException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();

        // set up empty ParsedDiscoveryRedirect
        ParsedDiscoveryRedirect parsedDiscoveryRedirect = new ParsedDiscoveryRedirect(null, null, null);
        mockParseDiscoveryRedirectSuccess(mockedDiscovery, parsedDiscoveryRedirect);
        DiscoveryModel.getInstance().setDiscoveryServiceRedirectedURL("url");
        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnDiscoveryRedirect(config);

        // THEN
        assertTrue(status.isStartDiscovery());
    }

    @Test
    public void
    callMobileConnectOnDiscoveryRedirect_whenCompleteSelectedOperatorDiscoveryThrowsDiscoveryException_returnedStatusShouldBeError() throws
                                                                                                                                                 DiscoveryException,
                                                                                                                                                 URISyntaxException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();

        // set up a valid ParsedDiscoveryRedirect
        ParsedDiscoveryRedirect parsedDiscoveryRedirect = new ParsedDiscoveryRedirect("901", "01", null);
        mockParseDiscoveryRedirectSuccess(mockedDiscovery, parsedDiscoveryRedirect);
        connect.setDiscovery(mockedDiscovery);
        // set up exception
        DiscoveryException expectedException = new DiscoveryException("Test");
        mockCompleteSelectedOperatorDiscoveryThrowsException(mockedDiscovery, expectedException);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnDiscoveryRedirect(config);

        // THEN
        assertTrue(status.isError());
        assertEquals(expectedException, status.getException());
        assertEquals("internal error", status.getError());
        assertEquals("Failed to obtain operator details.", status.getDescription());
    }

    @Test
    public void
    callMobileConnectOnDiscoveryRedirect_whenCompleteSelectedOperatorDiscoveryResultsInError_returnedStatusShouldBeError() throws
                                                                                                                                       DiscoveryException,
                                                                                                                                       URISyntaxException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();

        // set up valid ParsedDiscoveryRedirect
        ParsedDiscoveryRedirect parsedDiscoveryRedirect = new ParsedDiscoveryRedirect("901", "01", null);
        mockParseDiscoveryRedirectSuccess(mockedDiscovery, parsedDiscoveryRedirect);

        // Set up error discovery response
        int responseCode = 404;
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, responseCode, null, null);
        mockCompleteSelectedOperatorDiscoverySuccess(mockedDiscovery, expectedDiscoveryResponse);

        // Set up error response
        String expectedError = "ERROR";
        String expectedErrorDescription = "ERROR-DESCRIPTION";
        mockGetErrorResponse(mockedDiscovery, expectedDiscoveryResponse, expectedError, expectedErrorDescription);
        connect.setDiscovery(mockedDiscovery);
        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnDiscoveryRedirect(config);

        // THEN
        assertTrue(status.isError());
        assertEquals(expectedError, status.getError());
        assertEquals(expectedErrorDescription, status.getDescription());
        assertEquals(expectedDiscoveryResponse, status.getDiscoveryResponse());
    }

    @Test
    public void
    callMobileConnectOnDiscoveryRedirect_whenCompleteSelectedOperatorDiscoveryResultsInOperatorNotIdentified_returnedStatusShouldBeStartDiscovery() throws
                                                                                                                                                                DiscoveryException,
                                                                                                                                                                URISyntaxException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();

        // set up valid ParsedDiscoveryRedirect
        ParsedDiscoveryRedirect parsedDiscoveryRedirect = new ParsedDiscoveryRedirect("901", "01", null);
        mockParseDiscoveryRedirectSuccess(mockedDiscovery, parsedDiscoveryRedirect);

        // set up empty response
        DiscoveryResponse discoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        mockCompleteSelectedOperatorDiscoverySuccess(mockedDiscovery, discoveryResponse);

        // set up operator selection required
        mockIsOperatorSelectionRequired(mockedDiscovery, discoveryResponse, true);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnDiscoveryRedirect(config);

        // THEN
        assertTrue(status.isStartDiscovery());
    }

    @Test
    public void
    callMobileConnectOnDiscoveryRedirect_whenCompleteSelectedOperatorDiscoveryResultsInAuthorization_returnedStatusShouldBeStartAuthorization() throws
                                                                                                                                                            DiscoveryException,
                                                                                                                                                            URISyntaxException
    {
        // GIVEN
        IDiscovery mockedDiscovery = mock(IDiscovery.class);

        DiscoveryService connect = new DiscoveryService();

        // set up valid ParsedDiscoveryRedirect
        ParsedDiscoveryRedirect parsedDiscoveryRedirect = new ParsedDiscoveryRedirect("901", "01", null);
        mockParseDiscoveryRedirectSuccess(mockedDiscovery, parsedDiscoveryRedirect);

        // set up cached (valid) response
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(true, null, 0, null, null);
        mockCompleteSelectedOperatorDiscoverySuccess(mockedDiscovery, expectedDiscoveryResponse);

        // set up operator identified
        mockIsOperatorSelectionRequired(mockedDiscovery, expectedDiscoveryResponse, false);
        connect.setDiscovery(mockedDiscovery);
        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnDiscoveryRedirect(config);

        // THEN
        assertTrue(status.isStartAuthorization());
        assertEquals(expectedDiscoveryResponse, status.getDiscoveryResponse());
    }

    @Test
    public void
    callMobileConnectOnAuthorizationRedirect_whenNoDiscoveryInSession_returnedStatusShouldBeStartDiscovery() throws
                                                                                                                         OIDCException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService();


        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config, null);

        // THEN
        assertTrue(status.isStartDiscovery());
    }

    @Test
    public void
    callMobileConnectOnAuthorizationRedirect_whenParsedAuthorizationResponseHasError_returnedStatusShouldBeError()
            throws
                                                                                                                               OIDCException,
                                                                                                                               DiscoveryResponseExpiredException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService(mockedOIDC);

        // set up valid session
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        MobileConnectState sessionState = new MobileConnectState(expectedDiscoveryResponse, null, null, null);


        // set up error ParsedAuthorizationResponse
        ParsedAuthorizationResponse parsedAuthorizationResponse = new ParsedAuthorizationResponse();
        String expectedError = "ERROR";
        String expectedErrorDescription = "ERROR-DESCRIPTION";
        parsedAuthorizationResponse.set_error(expectedError);
        parsedAuthorizationResponse.set_error_description(expectedErrorDescription);
        mockParseAuthenticationResponseSuccess(mockedOIDC, parsedAuthorizationResponse);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config,
                                                                                      expectedDiscoveryResponse);

        // THEN
        assertTrue(status.isError());
        assertEquals(expectedError, status.getError());
        assertEquals(expectedErrorDescription, status.getDescription());
    }

    @Test
    public void callMobileConnectOnAuthorizationRedirect_whenStatesDoNotMatch_returnedStatusShouldBeError() throws
                                                                                                            OIDCException,
                                                                                                            DiscoveryResponseExpiredException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService(mockedOIDC);

        // set up valid session
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        MobileConnectState sessionState = new MobileConnectState(expectedDiscoveryResponse, null, "VALUE1", null);


        // set up valid ParsedAuthorizationResponse
        ParsedAuthorizationResponse parsedAuthorizationResponse = new ParsedAuthorizationResponse();
        parsedAuthorizationResponse.set_state("VALUE2");
        mockParseAuthenticationResponseSuccess(mockedOIDC, parsedAuthorizationResponse);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config,
                                                                                      expectedDiscoveryResponse);

        // THEN
        assertTrue(status.isError());
        assertEquals("Invalid authentication response", status.getError());
        assertEquals("State values do not match", status.getDescription());
    }

    @Test
    public void
    callMobileConnectOnAuthorizationRedirect_whenRequestTokenDiscoveryResponseExpired_returnedStatusShouldBeStartDiscovery() throws
                                                                                                                                         OIDCException,
                                                                                                                                         DiscoveryResponseExpiredException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService(mockedOIDC);

        // set up valid session
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        String state = config.getAuthorizationState();
        MobileConnectState sessionState = new MobileConnectState(expectedDiscoveryResponse, null, state, null);


        // set up valid ParsedAuthorizationResponse
        ParsedAuthorizationResponse parsedAuthorizationResponse = new ParsedAuthorizationResponse();
        parsedAuthorizationResponse.set_state(state);
        mockParseAuthenticationResponseSuccess(mockedOIDC, parsedAuthorizationResponse);

        // set up requestToken to throw exception
        mockRequestTokenThrowsException(mockedOIDC, new DiscoveryResponseExpiredException("TEST"));

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config,
                                                                                      expectedDiscoveryResponse);

        // THEN
        assertTrue(status.isStartDiscovery());
    }

    @Test
    public void callMobileConnectOnAuthorizationRedirect_whenRequestThrowsOIDCException_returnedStatusShouldBeError()
            throws
                                                                                                                      OIDCException,
                                                                                                                      DiscoveryResponseExpiredException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService(mockedOIDC);

        // set up valid session
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        String state = config.getAuthorizationState();
        MobileConnectState sessionState = new MobileConnectState(expectedDiscoveryResponse, null, state, null);


        // set up valid ParsedAuthorizationResponse
        ParsedAuthorizationResponse parsedAuthorizationResponse = new ParsedAuthorizationResponse();
        parsedAuthorizationResponse.set_state(state);
        mockParseAuthenticationResponseSuccess(mockedOIDC, parsedAuthorizationResponse);

        // set up OIDCException
        OIDCException expectedException = new OIDCException("TEST");
        mockRequestTokenThrowsException(mockedOIDC, expectedException);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config,
                                                                                      expectedDiscoveryResponse);

        // THEN
        assertTrue(status.isError());

        assertEquals("Failed to obtain a token.", status.getError());
        assertEquals("Failed to obtain an authentication token from the operator.", status.getDescription());
        assertEquals(expectedException, status.getException());
    }

    @Test
    public void callMobileConnectOnAuthorizationRedirect_whenFailedResponseCode_returnedStatusShouldBeError() throws
                                                                                                              OIDCException,
                                                                                                              DiscoveryResponseExpiredException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService(mockedOIDC);

        // set up valid session
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        String state = config.getAuthorizationState();
        MobileConnectState sessionState = new MobileConnectState(expectedDiscoveryResponse, null, state, null);


        // set up ParsedAuthorizationResponse
        ParsedAuthorizationResponse expectedParsedAuthorizationResponse = new ParsedAuthorizationResponse();
        expectedParsedAuthorizationResponse.set_state(state);
        mockParseAuthenticationResponseSuccess(mockedOIDC, expectedParsedAuthorizationResponse);

        // set up RequestTokenResponse with failed response code
        RequestTokenResponse expectedRequestTokenResponse = new RequestTokenResponse();
        int expectedResponseCode = 404; // Failed response code
        expectedRequestTokenResponse.setResponseCode(expectedResponseCode);
        ErrorResponse errorResponse = new ErrorResponse();
        String expectedError = "ERROR";
        String expectedErrorDescription = "ERROR-DESCRIPTION";
        errorResponse.set_error(expectedError);
        errorResponse.set_error_description(expectedErrorDescription);
        expectedRequestTokenResponse.setErrorResponse(errorResponse);
        mockRequestTokenSuccess(mockedOIDC, expectedRequestTokenResponse);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config,
                                                                                      expectedDiscoveryResponse);

        // THEN
        assertTrue(status.isError());

        assertEquals(expectedError, status.getError());
        assertEquals(expectedErrorDescription, status.getDescription());
        assertEquals(expectedParsedAuthorizationResponse, status.getParsedAuthorizationResponse());
        assertEquals(expectedRequestTokenResponse, status.getRequestTokenResponse());
    }

    @Test
    public void callMobileConnectOnAuthorizationRedirect_whenErrorTokenResponse_returnedStatusShouldBeError() throws
                                                                                                              OIDCException,
                                                                                                              DiscoveryResponseExpiredException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService(mockedOIDC);

        // set up valid session
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        String state = config.getAuthorizationState();
        MobileConnectState sessionState = new MobileConnectState(expectedDiscoveryResponse, null, state, null);


        // set up valid ParsedAuthorizationResponse
        ParsedAuthorizationResponse expectedParsedAuthorizationResponse = new ParsedAuthorizationResponse();
        expectedParsedAuthorizationResponse.set_state(state);
        mockParseAuthenticationResponseSuccess(mockedOIDC, expectedParsedAuthorizationResponse);

        // set up RequestTokenResponse with error response
        RequestTokenResponse expectedRequestTokenResponse = new RequestTokenResponse();
        int expectedResponseCode = 200;
        expectedRequestTokenResponse.setResponseCode(expectedResponseCode);
        ErrorResponse errorResponse = new ErrorResponse();
        String expectedError = "ERROR-ALT";
        String expectedErrorDescription = "ERROR-DESCRIPTION-ALT";
        errorResponse.set_error(expectedError);
        errorResponse.set_error_description(expectedErrorDescription);
        expectedRequestTokenResponse.setErrorResponse(errorResponse);
        mockRequestTokenSuccess(mockedOIDC, expectedRequestTokenResponse);


        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config,
                                                                                      expectedDiscoveryResponse);

        // THEN
        assertTrue(status.isError());
        assertEquals(expectedError, status.getError());
        assertEquals(expectedErrorDescription, status.getDescription());
        assertEquals(expectedParsedAuthorizationResponse, status.getParsedAuthorizationResponse());
        assertEquals(expectedRequestTokenResponse, status.getRequestTokenResponse());
    }

    @Test
    public void callMobileConnectOnAuthorizationRedirect_whenRequestTokenSucceeds_returnedStatusShouldBeComplete()
            throws
                                                                                                                   OIDCException,
                                                                                                                   DiscoveryResponseExpiredException
    {
        // GIVEN
        IOIDC mockedOIDC = mock(IOIDC.class);

        AuthorizationService connect = new AuthorizationService(mockedOIDC);

        // set up valid session
        DiscoveryResponse expectedDiscoveryResponse = new DiscoveryResponse(false, null, 200, null, null);
        String state = config.getAuthorizationState();
        MobileConnectState sessionState = new MobileConnectState(expectedDiscoveryResponse, null, state, null);


        // set up valid ParsedAuthorizationResponse
        ParsedAuthorizationResponse expectedParsedAuthorizationResponse = new ParsedAuthorizationResponse();
        expectedParsedAuthorizationResponse.set_state(state);
        mockParseAuthenticationResponseSuccess(mockedOIDC, expectedParsedAuthorizationResponse);

        // set up successful RequestTokenResponse
        RequestTokenResponse expectedRequestTokenResponse = new RequestTokenResponse();
        int expectedResponseCode = 200;
        expectedRequestTokenResponse.setResponseCode(expectedResponseCode);
        mockRequestTokenSuccess(mockedOIDC, expectedRequestTokenResponse);

        // WHEN
        MobileConnectStatus status = connect.callMobileConnectOnAuthorizationRedirect(config,
                                                                                      expectedDiscoveryResponse);

        // THEN
        assertTrue(status.isComplete());
        assertEquals(expectedParsedAuthorizationResponse, status.getParsedAuthorizationResponse());
        assertEquals(expectedRequestTokenResponse, status.getRequestTokenResponse());
    }

    @Test
    public void generateUniqueString_withPrefix_shouldReturnStringWithPrefix()
    {
        // GIVEN
        String expectedPrefix = "EXPECTED_PREFIX_";
        AuthorizationService connect = new AuthorizationService();

        // WHEN
        String uniqueString = connect.generateUniqueString(expectedPrefix);

        // THEN
        assertTrue(uniqueString.startsWith(expectedPrefix));
        assertTrue(uniqueString.length() > expectedPrefix.length());
    }

    @Test
    public void generateUniqueString_withoutPrefix_shouldNotFail()
    {
        // GIVEN

        AuthorizationService connect = new AuthorizationService();
        // WHEN
        String uniqueString = connect.generateUniqueString(null);

        // THEN
        assertTrue(uniqueString.length() > 0);
    }

    @Test
    public void hasMatchingState_requestNullResponseNull_shouldReturnTrue()
    {
        // GIVEN

        AuthorizationService connect = new AuthorizationService();
        // WHEN
        boolean matchingState = connect.hasMatchingState(null, null);

        // THEN
        assertTrue(matchingState);
    }

    @Test
    public void hasMatchingState_requestNotNullResponseNull_shouldReturnFalse()
    {
        // GIVEN

        AuthorizationService connect = new AuthorizationService();
        // WHEN
        boolean matchingState = connect.hasMatchingState("VALUE", null);

        // THEN
        assertFalse(matchingState);
    }

    @Test
    public void hasMatchingState_requestNullResponseNotNull_shouldReturnFalse()
    {
        // GIVEN

        AuthorizationService connect = new AuthorizationService();
        // WHEN
        boolean matchingState = connect.hasMatchingState(null, "VALUE");

        // THEN
        assertFalse(matchingState);
    }

    @Test
    public void hasMatchingState_differentValues_shouldReturnFalse()
    {
        // GIVEN

        AuthorizationService connect = new AuthorizationService();
        // WHEN
        boolean matchingState = connect.hasMatchingState("VALUE1", "VALUE2");

        // THEN
        assertFalse(matchingState);
    }

    @Test
    public void hasMatchingState_equalValues_shouldReturnFalse()
    {
        // GIVEN

        AuthorizationService connect = new AuthorizationService();
        // WHEN
        boolean matchingState = connect.hasMatchingState("VALUE", "VALUE");

        // THEN
        assertTrue(matchingState);
    }

    @Test
    public void testListenerSuccess()
    {
        MobileConnectStatus mobileConnectStatus = mock(MobileConnectStatus.class);
        RequestTokenResponse response = mock(RequestTokenResponse.class);
        RequestTokenResponseData responseData = mock(RequestTokenResponseData.class);
        ParsedIdToken parsedIdToken = mock(ParsedIdToken.class);

        AuthorizationListener listener = mock(AuthorizationListener.class);

        when(mobileConnectStatus.isComplete()).thenReturn(true);

        when(mobileConnectStatus.getRequestTokenResponse()).thenReturn(response);
        when(response.getResponseData()).thenReturn(responseData);
        when(responseData.getParsedIdToken()).thenReturn(parsedIdToken);
        when(parsedIdToken.get_pcr()).thenReturn("token");

        AuthorizationService connect = new AuthorizationService();
        connect.notifyListener(mobileConnectStatus, listener);

        verify(listener, times(1)).tokenReceived(Mockito.any(RequestTokenResponse.class));
    }

    @Test
    public void testListenerError()
    {
        MobileConnectStatus mobileConnectStatus = mock(MobileConnectStatus.class);
        RequestTokenResponse response = mock(RequestTokenResponse.class);
        RequestTokenResponseData responseData = mock(RequestTokenResponseData.class);
        ParsedIdToken parsedIdToken = mock(ParsedIdToken.class);

        AuthorizationListener listener = mock(AuthorizationListener.class);

        when(mobileConnectStatus.isError()).thenReturn(true);

        AuthorizationService connect = new AuthorizationService();
        connect.notifyListener(mobileConnectStatus, listener);

        verify(listener, times(1)).authorizationFailed(mobileConnectStatus);
        verify(listener, times(0)).tokenReceived(Mockito.any(RequestTokenResponse.class));
    }

    @Test
    public void testListenerDiscovery()
    {
        MobileConnectStatus mobileConnectStatus = mock(MobileConnectStatus.class);
        RequestTokenResponse response = mock(RequestTokenResponse.class);
        RequestTokenResponseData responseData = mock(RequestTokenResponseData.class);
        ParsedIdToken parsedIdToken = mock(ParsedIdToken.class);

        AuthorizationListener listener = mock(AuthorizationListener.class);

        when(mobileConnectStatus.isStartDiscovery()).thenReturn(true);

        AuthorizationService connect = new AuthorizationService();
        connect.notifyListener(mobileConnectStatus, listener);

        verify(listener, times(1)).authorizationFailed(mobileConnectStatus);
        verify(listener, times(0)).tokenReceived(Mockito.any(RequestTokenResponse.class));
    }

    private void mockStartAutomatedOperatorDiscoverySuccess(IDiscovery mockedDiscovery,
                                                            final DiscoveryResponse discoveryResponse) throws
                                                                                                       DiscoveryException
    {
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                IDiscoveryResponseCallback callback = (IDiscoveryResponseCallback) args[4];
                callback.completed(discoveryResponse);
                return null;
            }
        }).when(mockedDiscovery)
          .startAutomatedOperatorDiscovery(any(IPreferences.class),
                                           anyString(),
                                           any(DiscoveryOptions.class),
                                           Matchers.<List<KeyValuePair>>any(),
                                           any(IDiscoveryResponseCallback.class));
    }

    private void mockStartAutomatedOperatorDiscoveryThrowException(IDiscovery mockedDiscovery,
                                                                   DiscoveryException expectedException) throws
                                                                                                         DiscoveryException
    {
        doThrow(expectedException).when(mockedDiscovery)
                                  .startAutomatedOperatorDiscovery(any(IPreferences.class),
                                                                   anyString(),
                                                                   any(DiscoveryOptions.class),
                                                                   Matchers.<List<KeyValuePair>>any(),
                                                                   any(IDiscoveryResponseCallback.class));
    }

    private void mockExtractOperatorSelectionURL(IDiscovery mockedDiscovery, String expectedUrl)
    {
        when(mockedDiscovery.extractOperatorSelectionURL(any(DiscoveryResponse.class))).thenReturn(expectedUrl);
    }

    private void mockParseDiscoveryRedirectSuccess(IDiscovery mockedDiscovery,
                                                   final ParsedDiscoveryRedirect parsedDiscoveryRedirect) throws
                                                                                                          URISyntaxException
    {
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                IParsedDiscoveryRedirectCallback callback = (IParsedDiscoveryRedirectCallback) args[1];
                callback.completed(parsedDiscoveryRedirect);
                return null;
            }
        }).when(mockedDiscovery).parseDiscoveryRedirect(anyString(), any(IParsedDiscoveryRedirectCallback.class));
    }

    private void mockParseDiscoveryRedirectThrowsException(IDiscovery mockedDiscovery,
                                                           URISyntaxException expectedException) throws
                                                                                                 URISyntaxException
    {
        doThrow(expectedException).when(mockedDiscovery)
                                  .parseDiscoveryRedirect(anyString(), any(IParsedDiscoveryRedirectCallback.class));
    }

    private void mockCompleteSelectedOperatorDiscoverySuccess(IDiscovery mockedDiscovery,
                                                              final DiscoveryResponse discoveryResponse) throws
                                                                                                         DiscoveryException
    {
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                IDiscoveryResponseCallback callback = (IDiscoveryResponseCallback) args[6];
                callback.completed(discoveryResponse);
                return null;
            }
        }).when(mockedDiscovery)
          .completeSelectedOperatorDiscovery(any(IPreferences.class),
                                             anyString(),
                                             anyString(),
                                             anyString(),
                                             any(CompleteSelectedOperatorDiscoveryOptions.class),
                                             Matchers.<List<KeyValuePair>>any(),
                                             any(IDiscoveryResponseCallback.class));
    }

    private void mockCompleteSelectedOperatorDiscoveryThrowsException(IDiscovery mockedDiscovery,
                                                                      DiscoveryException expectedException) throws
                                                                                                            DiscoveryException
    {
        doThrow(expectedException).when(mockedDiscovery)
                                  .completeSelectedOperatorDiscovery(any(IPreferences.class),
                                                                     anyString(),
                                                                     anyString(),
                                                                     anyString(),
                                                                     any(CompleteSelectedOperatorDiscoveryOptions
                                                                                 .class),
                                                                     Matchers.<List<KeyValuePair>>any(),
                                                                     any(IDiscoveryResponseCallback.class));
    }

    private void mockIsOperatorSelectionRequired(IDiscovery mockedDiscovery,
                                                 DiscoveryResponse discoveryResponse,
                                                 boolean t)
    {
        when(mockedDiscovery.isOperatorSelectionRequired(discoveryResponse)).thenReturn(t);
    }

    private void mockStartAuthenticationSuccess(IOIDC mockedOIDC,
                                                final StartAuthenticationResponse startAuthenticationResponse) throws
                                                                                                               OIDCException,
                                                                                                               DiscoveryResponseExpiredException
    {
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                IStartAuthenticationCallback callback = (IStartAuthenticationCallback) args[9];
                callback.complete(startAuthenticationResponse);
                return null;
            }
        }).when(mockedOIDC)
          .startAuthentication(any(DiscoveryResponse.class),
                               anyString(),
                               anyString(),
                               anyString(),
                               anyString(),
                               anyInt(),
                               anyString(),
                               anyString(),
                               any(AuthenticationOptions.class),
                               any(IStartAuthenticationCallback.class));
    }

    private void mockStartAuthenticationThrowsException(IOIDC mockedOIDC, Exception expectedException) throws
                                                                                                       OIDCException,
                                                                                                       DiscoveryResponseExpiredException
    {
        doThrow(expectedException).when(mockedOIDC)
                                  .startAuthentication(any(DiscoveryResponse.class),
                                                       anyString(),
                                                       anyString(),
                                                       anyString(),
                                                       anyString(),
                                                       anyInt(),
                                                       anyString(),
                                                       anyString(),
                                                       any(AuthenticationOptions.class),
                                                       any(IStartAuthenticationCallback.class));
    }

    private void mockParseAuthenticationResponseSuccess(IOIDC mockedOIDC,
                                                        final ParsedAuthorizationResponse
                                                                parsedAuthorizationResponse) throws
                                                                                                                       OIDCException
    {
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                IParseAuthenticationResponseCallback callback = (IParseAuthenticationResponseCallback) args[1];
                callback.complete(parsedAuthorizationResponse);
                return null;
            }
        }).when(mockedOIDC).parseAuthenticationResponse(anyString(), any(IParseAuthenticationResponseCallback.class));
    }

    private void mockRequestTokenSuccess(IOIDC mockedOIDC, final RequestTokenResponse requestTokenResponse) throws
                                                                                                            OIDCException,
                                                                                                            DiscoveryResponseExpiredException
    {
        doAnswer(new Answer()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Object[] args = invocationOnMock.getArguments();
                IRequestTokenCallback callback = (IRequestTokenCallback) args[4];
                callback.complete(requestTokenResponse);
                return null;
            }
        }).when(mockedOIDC)
          .requestToken(any(DiscoveryResponse.class),
                        anyString(),
                        anyString(),
                        any(TokenOptions.class),
                        any(IRequestTokenCallback.class));
    }

    private void mockRequestTokenThrowsException(IOIDC mockedOIDC, Exception ex) throws
                                                                                 OIDCException,
                                                                                 DiscoveryResponseExpiredException
    {
        doThrow(ex).when(mockedOIDC)
                   .requestToken(any(DiscoveryResponse.class),
                                 anyString(),
                                 anyString(),
                                 any(TokenOptions.class),
                                 any(IRequestTokenCallback.class));
    }

    private void mockGetErrorResponse(IDiscovery mockedDiscovery,
                                      DiscoveryResponse discoveryResponse,
                                      String expectedError,
                                      String expectedErrorDescription)
    {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.set_error(expectedError);
        errorResponse.set_error_description(expectedErrorDescription);
        when(mockedDiscovery.getErrorResponse(discoveryResponse)).thenReturn(errorResponse);
    }
}
