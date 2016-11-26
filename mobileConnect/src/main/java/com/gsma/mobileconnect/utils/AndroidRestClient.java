package com.gsma.mobileconnect.utils;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An Android specific version which overrides the incompatible aspects of the Java API.
 * Created by nick.copley on 26/02/2016.
 */
public class AndroidRestClient extends RestClient {

    @Override
    public HttpClientContext getHttpClientContext(String username, String password, URI uriForRealm) {
        String host = uriForRealm.getHost();
        int port = uriForRealm.getPort();
        if(port == -1) {
            if("http".equalsIgnoreCase(uriForRealm.getScheme())) {
                port = 80;
            } else if("https".equalsIgnoreCase(uriForRealm.getScheme())) {
                port = 443;
            }
        }

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host, port), new UsernamePasswordCredentials(username, password));
        BasicAuthCache authCache = new BasicAuthCache();
        authCache.put(new HttpHost(host, port, uriForRealm.getScheme()), new BasicScheme());
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);
        return context;
    }

    @Override
    public RestResponse callRestEndPoint(final HttpRequestBase httpRequest, HttpClientContext context, int timeout, List<KeyValuePair> cookiesToProxy) throws RestException, IOException {
        CookieStore cookieStore = this.buildCookieStore(httpRequest.getURI().getHost(), cookiesToProxy);

        RestResponse restResponse;
        try(CloseableHttpClient closeableHttpClient = this.getHttpClient(cookieStore)) {

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    httpRequest.abort();
                }
            }, (long) timeout);
            RequestConfig localConfig = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).setSocketTimeout(timeout).setCookieSpec("standard").build();
            context.setRequestConfig(localConfig);

            CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpRequest, context);
            restResponse = this.buildRestResponse(httpRequest, httpResponse);
            checkRestResponse(restResponse);
        }
        catch (IOException ioe) {
                String requestUri = httpRequest.getURI().toString();
                if(httpRequest.isAborted()) {
                    throw new RestException("Rest end point did not respond", requestUri);
                } else {
                    throw new RestException("Rest call failed", requestUri, ioe);
                }
            }

        return restResponse;
    }


    private CookieStore buildCookieStore(String host, List<KeyValuePair> cookiesToProxy) {
        BasicCookieStore cookieStore = new BasicCookieStore();
        if(cookiesToProxy == null) {
            return cookieStore;
        } else {

            for(KeyValuePair cookieToProxy : cookiesToProxy) {
                BasicClientCookie cookie = new BasicClientCookie(cookieToProxy.getKey(), cookieToProxy.getValue());
                cookie.setDomain(host);
                cookieStore.addCookie(cookie);
            }

            return cookieStore;
        }
    }

    private CloseableHttpClient getHttpClient(CookieStore cookieStore) {
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        return httpClient;
    }


    private RestResponse buildRestResponse(HttpRequestBase request, CloseableHttpResponse closeableHttpResponse) throws IOException {
        String requestUri = request.getURI().toString();
        int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();
        HeaderIterator headers = closeableHttpResponse.headerIterator();
        ArrayList headerList = new ArrayList(3);

        while(headers.hasNext()) {
            Header httpEntity = headers.nextHeader();
            headerList.add(new KeyValuePair(httpEntity.getName(), httpEntity.getValue()));
        }

        HttpEntity entity = closeableHttpResponse.getEntity();
        String responseData = EntityUtils.toString(entity);
        return new RestResponse(requestUri, statusCode, headerList, responseData);
    }

    private static void checkRestResponse(RestResponse response) throws RestException {
        if(!response.isJsonContent()) {
            throw new RestException("Invalid response", response);
        }
    }
}
