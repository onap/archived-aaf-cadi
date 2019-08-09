/**
 * ﻿============LICENSE_START=======================================================
 * org.onap.aaf
 * ================================================================================
 * Copyright © 2018 European Software Marketing Ltd.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aaf.cadi.sidecar.fproxy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.http.Cookie;

import org.eclipse.jetty.util.security.Password;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aaf.cadi.sidecar.fproxy.data.CredentialCacheData.CredentialType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FProxyServiceTest {

    static {
        System.setProperty("server.ssl.key-store-password",
                Password.deobfuscate("OBF:1y0q1uvc1uum1uvg1pil1pjl1uuq1uvk1uuu1y10"));
        System.setProperty("server.ssl.trust-store-password",
                Password.deobfuscate("OBF:1y0q1uvc1uum1uvg1pil1pjl1uuq1uvk1uuu1y10"));
    }

    @Value("${transactionid.header.name}")
    private String transactionIdHeaderName;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testRequestFrowarding() throws Exception {
        String testUrl = "https://localhost:80/testurl";
        String testResponse = "Response from MockRestService";
        String testTransactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";

        mockServer.expect(requestTo(testUrl)).andExpect(method(HttpMethod.GET))
                .andExpect(header(transactionIdHeaderName, testTransactionId))
                .andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));

        mvc.perform(MockMvcRequestBuilders.get(testUrl).accept(MediaType.APPLICATION_JSON)
                .header(transactionIdHeaderName, testTransactionId)).andExpect(status().isOk())
                .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();
    }

    @Test
    public void testCredentialCacheEndpoint() throws Exception {
        populateCredentialCache("tx1", "headername", "headervalue", CredentialType.HEADER.toString());
    }

    @Test
    public void testPopulateHeaderFromCache() throws Exception {
        String testTransactionId = "tx1";
        String testUrl = "https://localhost:80/testurl";
        String headerName = "headername";
        String headerValue = "headervalue";

        String testResponse = "Response from MockRestService";

        // Populate the cache with header credentials
        populateCredentialCache(testTransactionId, headerName, headerValue, CredentialType.HEADER.toString());

        // Expect mock server to be called with request containing cached header
        mockServer.expect(requestTo(testUrl)).andExpect(method(HttpMethod.GET))
                .andExpect(header(transactionIdHeaderName, testTransactionId))
                .andExpect(header(headerName, headerValue))
                .andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));

        // Send request to mock server with transaction Id
        mvc.perform(MockMvcRequestBuilders.get(testUrl).accept(MediaType.APPLICATION_JSON)
                .header(transactionIdHeaderName, testTransactionId)).andExpect(status().isOk())
                .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();
    }

    @Test
    public void testHeaderAlreadyExists() throws Exception {
        String testTransactionId = "tx1";
        String testUrl = "https://localhost:80/testurl";
        String headerName = "headername";
        String headerValue = "headervalue";
        String newHeaderValue = "newheadervalue";

        String testResponse = "Response from MockRestService";

        // Populate the cache with header credentials using a new value
        populateCredentialCache(testTransactionId, headerName, newHeaderValue, CredentialType.HEADER.toString());

        // Expect mock server to be called with request containing the original header credential value, not the cached
        // new header value
        mockServer.expect(requestTo(testUrl)).andExpect(method(HttpMethod.GET))
                .andExpect(header(headerName, headerValue))
                .andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));

        // Send request to mock server that already contains a header with same name as the one that has been cached
        mvc.perform(MockMvcRequestBuilders.get(testUrl).accept(MediaType.APPLICATION_JSON)
                .header(transactionIdHeaderName, testTransactionId).header(headerName, headerValue))
                .andExpect(status().isOk()).andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();
    }

    @Test
    public void testPopulateCookieFromCache() throws Exception {
        String testTransactionId = "tx1";
        String testUrl = "https://localhost:80/testurl";
        String cookieName = "testcookie";
        String cookieValue = "testcookie=testvalue";
        String testResponse = "Response from MockRestService";

        // Populate the cache with cookie credentials
        populateCredentialCache(testTransactionId, cookieName, cookieValue, CredentialType.COOKIE.toString());

        // Expect mock server to be called with request containing cached header
        mockServer.expect(requestTo(testUrl)).andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.COOKIE, cookieValue))
                .andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));

        // Send request to mock server with transaction Id
        mvc.perform(MockMvcRequestBuilders.get(testUrl).accept(MediaType.APPLICATION_JSON)
                .header(transactionIdHeaderName, testTransactionId)).andExpect(status().isOk())
                .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();
    }

    @Test
    public void testCookieAlreadyExists() throws Exception {
        String testTransactionId = "tx1";
        String testUrl = "https://localhost:80/testurl";
        String cookieName = "testcookie";
        String cookieValue = "testvalue";
        String newCookieValue = "newtestvalue";

        String testResponse = "Response from MockRestService";

        // Populate the cache with cookie credentials using a new value
        populateCredentialCache(testTransactionId, cookieName, newCookieValue, CredentialType.COOKIE.toString());

        // Expect mock server to be called with request containing the original cookie credential value, not the cached
        // new cookie value
        mockServer.expect(requestTo(testUrl)).andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.COOKIE, cookieName + "=" + cookieValue))
                .andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));

        // Send request to mock server that already contains a cookie with same name as the one that has been cached
        mvc.perform(MockMvcRequestBuilders.get(testUrl).accept(MediaType.APPLICATION_JSON)
                .header(transactionIdHeaderName, testTransactionId).cookie(new Cookie(cookieName, cookieValue)))
                .andExpect(status().isOk()).andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();
    }

    private void populateCredentialCache(String transactionId, String credentialName, String credentialValue,
            String credentialType) throws Exception {
        String cacheUrl = "https://localhost:80/credential-cache/" + transactionId;
        String requestBody = "{ \"credentialName\":\"" + credentialName + "\", \"credentialValue\":\"" + credentialValue
                + "\", \"credentialType\":\"" + credentialType + "\" }";

        // Populate the cache with credentials
        mvc.perform(MockMvcRequestBuilders.post(cacheUrl).content(requestBody).accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().string(equalTo(transactionId)));
    }
}
