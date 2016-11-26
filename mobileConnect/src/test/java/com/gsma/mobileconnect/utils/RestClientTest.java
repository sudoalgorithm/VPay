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

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.bootstrap.HttpServer;
//import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class RestClientTest
{
    class RequestTracker
    {
        private boolean receivedRequest;
        private boolean completedRequest;

        public boolean isReceivedRequest()
        {
            return receivedRequest;
        }

        public void setReceivedRequest(boolean receivedRequest)
        {
            this.receivedRequest = receivedRequest;
        }

        public boolean isCompletedRequest()
        {
            return completedRequest;
        }

        public void setCompletedRequest(boolean completedRequest)
        {
            this.completedRequest = completedRequest;
        }
    }

    /**
     * Start a basic local http server.
     *
     * @param requestTracker Track whether request is received and completed.
     * @param sleep Time to sleep after receiving request.
     * @param statusCode Status code to respond with.
     * @param contentTypeHeaderValue Value of content type header to respond with
     * @param responseContents The contents to respond with
     * @return A HttpServer instance (to allow server to be stopped).
     * @throws IOException
     */
    /*
    private HttpServer startServer (final RequestTracker requestTracker, final long sleep, final int statusCode,
                                    final String contentTypeHeaderValue, final String responseContents)
            throws IOException
    {
        HttpRequestHandler requestHandler = new HttpRequestHandler() {
            public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
                requestTracker.setReceivedRequest(true);
                if(sleep > 0)
                {
                    try
                    {
                        Thread.sleep(sleep);
                    }
                    catch (InterruptedException e)
                    {
                        //e.printStackTrace();
                    }
                }
                httpResponse.setStatusCode(statusCode);
                HttpEntity entity = new StringEntity(responseContents, ContentType.create(contentTypeHeaderValue));
                httpResponse.setEntity(entity);

                requestTracker.setCompletedRequest(true);
            }
        };
        HttpProcessor httpProcessor = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("Test-HTTP/1.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl())
                .build();
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(10000)
                .setTcpNoDelay(true)
                .build();
        final HttpServer server = ServerBootstrap.bootstrap()
                .setHttpProcessor(httpProcessor)
                .setSocketConfig(socketConfig)
                .registerHandler("*", requestHandler)
                .create();
        server.start();

        return server;
    }

    @Test
    public void callRestEndPoint_withBlockingServer_shouldTimeOut()
            throws IOException
    {
        // GIVEN
        // Set up a server that blocks when a request is received
        RequestTracker requestTracker = new RequestTracker();
        HttpServer server = startServer(requestTracker, 10000, 200, "application/json", "{}");

        String expectedUrl = null;
        try {
            int localPort = server.getLocalPort();

            expectedUrl = "http://localhost:" + localPort + "/";
            URI uri = new URI(expectedUrl);

            RestClient restClient = new RestClient();

            HttpClientContext context = restClient.getHttpClientContext("user", "password", uri);
            HttpGet get = new HttpGet(uri);

            // WHEN
            restClient.callRestEndPoint(get, context, 1000, null);

			// Expect exception to be thrown
            assertTrue(false);
        }
        catch (RestException ex)
        {
            // THEN
            // Exception is expected.
            assertTrue(requestTracker.isReceivedRequest());
            assertFalse(requestTracker.isCompletedRequest());

            assertEquals("Rest end point did not respond", ex.getMessage());
            assertEquals(expectedUrl, ex.getUri());
        }
        catch (Throwable ex)
        {
            // Unexpected exception thrown
            assertTrue(false);
        }
        finally
        {
            server.stop();
        }
    }

    @Test
    public void callRestEndPoint_withNonJsonResponse_shouldThrowException()
            throws IOException
    {
        // GIVEN
        // Set up a server that responds with non Json content.
        RequestTracker requestTracker = new RequestTracker();
        String expectedContents = "<html></html>";
        int expectedStatusCode = 404;
        HttpServer server = startServer(requestTracker, 0, expectedStatusCode, "text/html", expectedContents);

        String expectedUrl = null;
        try {
            int localPort = server.getLocalPort();

            expectedUrl = "http://localhost:" + localPort + "/";
            URI uri = new URI(expectedUrl);

            RestClient restClient = new RestClient();

            HttpClientContext context = restClient.getHttpClientContext("user", "password", uri);
            HttpGet get = new HttpGet(uri);

            // WHEN
            restClient.callRestEndPoint(get, context, 1000, null);

            // Expect exception to be thrown
            assertTrue(false);
        }
        catch (RestException ex)
        {
            // THEN
            // Exception is expected.
            assertTrue(requestTracker.isReceivedRequest());
            assertTrue(requestTracker.isCompletedRequest());

            assertEquals("Invalid response", ex.getMessage());
            assertEquals(expectedUrl, ex.getUri());
            assertEquals(expectedContents, ex.getContents());
            assertEquals(expectedStatusCode, ex.getStatusCode());
            assertNotNull(ex.getHeaders());
            assertTrue(ex.getHeaders().size() > 0);
        }
        catch (Throwable ex)
        {
            // Unexpected exception thrown
            assertTrue(false);
        }
        finally
        {
            server.stop();
        }
    }
/*
    @Test
    public void callRestEndPoint_withJsonResponse_shouldSucceed()
            throws IOException, URISyntaxException, RestException
    {
        // GIVEN
        // Set up a server that responds with Json.
        RequestTracker requestTracker = new RequestTracker();
        String expectedContents = "{ \"field\": \"value\" }";
        int expectedStatusCode = 200;
        HttpServer server = startServer(requestTracker, 0, expectedStatusCode, "application/json", expectedContents);

        try
        {
            int localPort = server.getLocalPort();

            String expectedUrl = "http://localhost:" + localPort + "/";
            URI uri = new URI(expectedUrl);

            RestClient restClient = new RestClient();

            HttpClientContext context = restClient.getHttpClientContext("user", "password", uri);
            HttpGet get = new HttpGet(uri);

            // WHEN
            RestResponse restResponse = restClient.callRestEndPoint(get, context, 1000, null);

            // THEN
            assertTrue(requestTracker.isReceivedRequest());
            assertTrue(requestTracker.isCompletedRequest());

            assertNotNull(restResponse);
            assertTrue(restResponse.isJsonContent());
            assertEquals(expectedUrl,restResponse.getUri());
            assertEquals(expectedStatusCode,restResponse.getStatusCode());
            assertEquals(expectedContents,restResponse.getResponse());
            assertNotNull(restResponse.getHeaders());
            assertTrue(restResponse.getHeaders().size() > 0);
        }
        finally
        {
            server.stop();
        }
    }
    **/
}
