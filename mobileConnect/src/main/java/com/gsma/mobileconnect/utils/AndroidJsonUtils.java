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

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.google.common.io.BaseEncoding;
import com.gsma.mobileconnect.helpers.UserInfo;
import com.gsma.mobileconnect.oidc.ParsedIdToken;
import com.gsma.mobileconnect.oidc.RequestTokenResponse;
import com.gsma.mobileconnect.oidc.RequestTokenResponseData;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Class to hold Json utility functions.
 */
public class AndroidJsonUtils
{
    private static final String TAG = AndroidJsonUtils.class.getName();

    /**
     * Extract the requested URL from the Operator Not Identified Discovery response.
     *
     * @param jsonDoc The json object to check.
     * @param relToFind The URL to find.
     * @return The requested URL if present, null otherwise.
     */
    public static String extractUrl(JsonNode jsonDoc, String relToFind)
    {
        if (null == jsonDoc)
        {
            throw new IllegalArgumentException("Missing argument jsonDoc");
        }
        if (null == relToFind)
        {
            throw new IllegalArgumentException("Missing argument relToFind");
        }

        JsonNode linksNode = jsonDoc.get(Constants.LINKS_FIELD_NAME);
        if(null == linksNode)
        {
            return null;
        }

        Iterator<JsonNode> i = linksNode.elements();
        while (i.hasNext())
        {
            JsonNode node = i.next();
            String rel = getOptionalStringValue(node, Constants.REL_FIELD_NAME);
            if (relToFind.equals(rel))
            {
                return getOptionalStringValue(node, Constants.HREF_FIELD_NAME);
            }
        }
        return null;
    }

    /**
     * Extract an error response from the discovery response if any.
     * <p>
     * A discovery response has an error if the error field is present.
     *
     * @param jsonDoc The discovery response to examine.
     * @return The error response if present, null otherwise.
     */
    public static ErrorResponse getErrorResponse(JsonNode jsonDoc)
    {
        if (null == jsonDoc)
        {
            throw new IllegalArgumentException("Missing argument jsonDoc");
        }

        String error = getOptionalStringValue(jsonDoc, Constants.ERROR_NAME);
        String errorDescription = getOptionalStringValue(jsonDoc, Constants.ERROR_DESCRIPTION_NAME);
        // Sometimes "description" rather than "error_description" is seen
        String altErrorDescription = getOptionalStringValue(jsonDoc, Constants.ERROR_DESCRIPTION_ALT_NAME);
        String errorUri = getOptionalStringValue(jsonDoc, Constants.ERROR_URI_NAME);

        if(StringUtils.isNullOrEmpty(error))
        {
            return null;
        }

        if(!StringUtils.isNullOrEmpty(altErrorDescription))
        {
            if (StringUtils.isNullOrEmpty(errorDescription))
            {
                errorDescription = altErrorDescription;
            }
            else
            {
                errorDescription += " " + altErrorDescription;
            }
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.set_error(error);
        errorResponse.set_error_description(errorDescription);
        errorResponse.set_error_uri(errorUri);
        return errorResponse;
    }

    /**
     * Parse the string into a Jackson Tree
     * <p>
     * Returns a missing node if the string is empty.
     *
     * @param jsonStr The Json string to parse.
     * @return The Jackson Json Tree
     * @throws IOException
     */
    public static JsonNode parseJson(String jsonStr)
            throws IOException
    {
        if(StringUtils.isNullOrEmpty(jsonStr))
        {
            return MissingNode.getInstance();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonStr);
        if (null == root)
        {
            return MissingNode.getInstance();
        }
        return root;
    }

    /**
     * Parse an Operator Identified Discovery Result.
     * <p>
     * If the discovery result looks like an Operator Identified result a ParsedOperatorIdentifiedDiscoveryResult is returned.
     *
     * @param jsonDoc The discovery result to examine.
     * @return The parsed operator discovery result or null.
     */
    public static ParsedOperatorIdentifiedDiscoveryResult parseOperatorIdentifiedDiscoveryResult(JsonNode jsonDoc)
    {
        if (null == jsonDoc)
        {
            throw new IllegalArgumentException("Missing parameter jsonDoc");
        }

        try
        {
            ParsedOperatorIdentifiedDiscoveryResult parsedOperatorIdentifiedDiscoveryResult = new ParsedOperatorIdentifiedDiscoveryResult();
            JsonNode responseNode = getExpectedNode(jsonDoc, Constants.RESPONSE_FIELD_NAME);

            parsedOperatorIdentifiedDiscoveryResult.setClientId(getExpectedStringValue(responseNode, Constants.CLIENT_ID_FIELD_NAME));
            parsedOperatorIdentifiedDiscoveryResult.setClientSecret(getExpectedStringValue(responseNode, Constants.CLIENT_SECRET_FIELD_NAME));

            JsonNode linkNode = responseNode.path(Constants.APIS_FIELD_NAME).path(Constants.OPERATORID_FIELD_NAME).path(Constants.LINK_FIELD_NAME);
            if(linkNode.isMissingNode())
            {
                return null;
            }

            Iterator<JsonNode> i = linkNode.elements();
            while (i.hasNext())
            {
                JsonNode node = i.next();
                String rel = getExpectedStringValue(node, Constants.REL_FIELD_NAME);
                if (Constants.AUTHORIZATION_REL.equals(rel))
                {
                    parsedOperatorIdentifiedDiscoveryResult.setAuthorizationHref(getExpectedStringValue(node, Constants.HREF_FIELD_NAME));
                }
                else if (Constants.TOKEN_REL.equals(rel))
                {
                    parsedOperatorIdentifiedDiscoveryResult.setTokenHref(getExpectedStringValue(node, Constants.HREF_FIELD_NAME));
                }
                else if (Constants.USER_INFO_REL.equals(rel))
                {
                    parsedOperatorIdentifiedDiscoveryResult.setUserInfoHref(getExpectedStringValue(node, Constants.HREF_FIELD_NAME));
                }
                else if (Constants.PREMIUM_INFO_REL.equals(rel))
                {
                    parsedOperatorIdentifiedDiscoveryResult.setPremiumInfoHref(getExpectedStringValue(node, Constants.HREF_FIELD_NAME));
                }
            }
            return parsedOperatorIdentifiedDiscoveryResult;
        }
        catch(NoFieldException ex)
        {
            return null;
        }
    }

    /**
     * Parse the response from a request token call.
     * <p>
     * The json is expected to be either an error or a successful request token response.
     *
     * @param timeReceived The time the response was received, used to timestamp the returned object.
     * @param jsonStr The Json string to examine.
     * @return The parsed response.
     * @throws IOException
     */
    public static RequestTokenResponse parseRequestTokenResponse(Calendar timeReceived, String jsonStr)
            throws IOException
    {
        RequestTokenResponse requestTokenResponse = new RequestTokenResponse();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonDoc = objectMapper.readTree(jsonStr);

        ErrorResponse errorResponse = getErrorResponse(jsonDoc);
        if(null != errorResponse)
        {
            requestTokenResponse.setErrorResponse(getErrorResponse(jsonDoc));
            return requestTokenResponse;
        }

        RequestTokenResponseData responseData = new RequestTokenResponseData();
        requestTokenResponse.setResponseData(responseData);

        responseData.setTimeReceived(timeReceived);
        responseData.setOriginalResponse(jsonStr);

        responseData.set_access_token(getOptionalStringValue(jsonDoc, Constants.ACCESS_TOKEN_FIELD_NAME));
        responseData.set_token_type(getOptionalStringValue(jsonDoc, Constants.TOKEN_TYPE_FIELD_NAME));
        responseData.set_refresh_token(getOptionalStringValue(jsonDoc, Constants.REFRESH_TOKEN_FIELD_NAME));
        Integer expiresIn = getOptionalIntegerValue(jsonDoc, Constants.EXPIRES_IN_FIELD_NAME);
        responseData.set_expires_in(expiresIn);
        String idTokenStr = getOptionalStringValue(jsonDoc, Constants.ID_TOKEN_FIELD_NAME);
        if(!StringUtils.isNullOrEmpty(idTokenStr))
        {
            ParsedIdToken parsedIdToken = createParsedIdToken(idTokenStr);
            responseData.setParsedIdToken(parsedIdToken);
        }
        return requestTokenResponse;
    }

    /**
     * Parse the passed string as an id token.
     *
     * @param idTokenStr The string to parse.
     * @return A ParsedIdToken.
     * @throws IOException
     */
    public static ParsedIdToken createParsedIdToken(String idTokenStr)
            throws IOException
    {
        IdToken idToken = parseIdToken(idTokenStr);

        ParsedIdToken parsedIdToken = new ParsedIdToken();
        parsedIdToken.set_id_token(idTokenStr);
        parsedIdToken.set_pcr(idToken.getPayload().get_sub());
        parsedIdToken.set_nonce(idToken.getPayload().get_nonce());
        parsedIdToken.set_id_token_claims(idToken.getPayload().getClaims());
        parsedIdToken.set_id_token_verified(false);

        return parsedIdToken;
    }

    /**
     * Parse the string as an id_token.
     *
     * @param idToken The string to parse.
     * @return An IdToken.
     * @throws IOException
     */
    private static IdToken parseIdToken(String idToken)
            throws IOException
    {
        String[] parts = idToken.split("\\.");
        if(parts.length != 3)
        {
            throw new IllegalArgumentException("Not an id_token");
        }

        String header = new String(BaseEncoding.base64().decode(parts[0]), "UTF-8");
        String payload = new String(BaseEncoding.base64().decode(parts[1]), "UTF-8");

        IdToken parsedIdToken = new IdToken();
        parsedIdToken.setHeader(createJwtHeader(header));
        parsedIdToken.setPayload(createJwtPayload(payload));
        parsedIdToken.setSignature(parts[2]);

        return parsedIdToken;
    }

    /**
     * Parse the string as a Jwt header.
     *
     * @param header The string to parse.
     * @return A Jwt header.
     * @throws IOException
     */
    static private JwtHeader createJwtHeader(String header)
            throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonDoc = objectMapper.readTree(header);

        JwtHeader parsedJwtHeader = new JwtHeader();
        parsedJwtHeader.set_typ(getOptionalStringValue(jsonDoc, Constants.TYP_FIELD_NAME));
        parsedJwtHeader.set_alg(getOptionalStringValue(jsonDoc, Constants.ALG_FIELD_NAME));

        return parsedJwtHeader;
    }

    /**
     * Parse the string as a Jwt payload.
     *
     * @param payload The string to parse.
     * @return A JwtPayload.
     * @throws IOException
     */
    static private JwtPayload createJwtPayload(String payload)
            throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonDoc = objectMapper.readTree(payload);

        JwtPayload parsedJwtPayload = new JwtPayload();
        parsedJwtPayload.setClaims(jsonDoc);

        return parsedJwtPayload;
    }

    /**
     * Query the parent node for the named child node.
     *
     * @param parentNode Node to check.
     * @param name Name of child node.
     * @return The child node if found.
     * @throws NoFieldException Thrown if field not found.
     */
    public static JsonNode getExpectedNode(JsonNode parentNode, String name)
            throws NoFieldException
    {
        JsonNode childNode = parentNode.get(name);
        if(null == childNode)
        {
            throw new NoFieldException(name);
        }
        return childNode;
    }

    /**
     * Query the parent node for the named child node and return the text value of the child node
     *
     * @param parentNode Node to check.
     * @param name Name of the child node.
     * @return The text value of the child node.
     * @throws NoFieldException Thrown if field not found.
     */
    public static String getExpectedStringValue(JsonNode parentNode, String name)
            throws NoFieldException
    {
        JsonNode childNode = parentNode.get(name);
        if(null == childNode)
        {
            throw new NoFieldException(name);
        }
        return childNode.textValue();
    }

    /**
     * Return the string value of an optional child node.
     * <p>
     * Check the parent node for the named child, if found return the string contents of the child node, return null otherwise.
     *
     * @param parentNode The node to check.
     * @param name Name of the optional child node.
     * @return Text value of child node, if found, null otherwise.
     */
    public static String getOptionalStringValue(JsonNode parentNode, String name)
    {
        JsonNode childNode = parentNode.get(name);
        if(null == childNode)
        {
            return null;
        }
        else
        {
            return childNode.textValue();
        }
    }

    /**
     * Return the integer value of an optional child node.
     * <p>
     * Check the parent node for the named child, if found return the integer contents of the child node, return null otherwise.
     *
     * @param parentNode The node to check
     * @param name The name of the optional child.
     * @return Integer value of the child node if present, null otherwise.
     */
    public static Integer getOptionalIntegerValue(JsonNode parentNode, String name)
    {
        JsonNode childNode = parentNode.get(name);
        if(null == childNode)
        {
            return null;
        }
        else
        {
            return childNode.asInt();
        }
    }

    /**
     * Return the long value of an optional child node.
     * <p>
     * Check the parent node for the named child, if found return the long contents of the child node, return null otherwise.
     *
     * @param parentNode The node to check
     * @param name The name of the optional child.
     * @return Long value of the child node if present, null otherwise.
     */
    public static Long getOptionalLongValue(JsonNode parentNode, String name)
    {
        JsonNode childNode = parentNode.get(name);
        if(null == childNode)
        {
            return null;
        }
        else
        {
            return childNode.asLong();
        }
    }

    /**
     * Return the ttl field in the Json object.
     *
     * @param node The Json object to examine.
     * @return The ttl value if present, null otherwise.
     */
    static public Long getDiscoveryResponseTtl(JsonNode node)
    {
        return getOptionalLongValue(node, Constants.TTL_FIELD_NAME);
    }

    /**
     * Parse the UserInfo object.
     * @param response
     * @return
     */
    public static UserInfo parseUserInfo(String response) {
        UserInfo userInfo = new UserInfo();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonDoc = null;
        try {
            jsonDoc = objectMapper.readTree(response);
            Log.d(TAG, jsonDoc.asText());

        } catch (IOException e) {
            e.printStackTrace();
        }

        ErrorResponse errorResponse = getErrorResponse(jsonDoc);
        if(null != errorResponse)
        {

            return null;
        }
        return userInfo;
    }
}
