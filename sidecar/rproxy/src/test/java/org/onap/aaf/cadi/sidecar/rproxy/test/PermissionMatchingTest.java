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
package org.onap.aaf.cadi.sidecar.rproxy.test;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.annotation.Resource;

import org.eclipse.jetty.util.security.Password;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aaf.cadi.sidecar.rproxy.config.ForwardProxyProperties;
import org.onap.aaf.cadi.sidecar.rproxy.config.PrimaryServiceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc

@TestPropertySource(locations = {"classpath:primary-service.properties", "classpath:forward-proxy.properties"})

@ContextConfiguration(classes = ReverseProxyTestConfig.class)
public class PermissionMatchingTest {
	
    static {
        System.setProperty("server.ssl.key-store-password",
                Password.deobfuscate("OBF:1y0q1uvc1uum1uvg1pil1pjl1uuq1uvk1uuu1y10"));
    }

    @Value("${transactionid.header.name}")
    private String transactionIdHeaderName;

    @Resource(name = "PrimaryServiceProperties")
    private PrimaryServiceProperties primaryServiceProps;

    @Resource(name = "ForwardProxyProperties")
    private ForwardProxyProperties forwardProxyProps;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    private String primaryServiceBaseUrl;
    
    @Before
    public void setUp() throws Exception {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        primaryServiceBaseUrl = primaryServiceProps.getProtocol() + "://" + primaryServiceProps.getHost() + ":"
                + primaryServiceProps.getPort();
    }
    
	@Test 
	public void testURIMismatch() throws Exception {
		
        String testUrl = "/uri/does/not/exist";
        String testResponse = "Sorry, the request is not allowed";
        
        mockMvc
        	.perform(get(testUrl))
        	.andExpect(status().isForbidden())
        	.andExpect(status().reason(testResponse)); 

	}
	
	@Test 
	public void testURINoPermission() throws Exception {
		
        String testUrl = "/not/allowed/at/all";
        String testResponse = "Sorry, the request is not allowed";
        
        mockMvc
        	.perform(get(testUrl))
        	.andExpect(status().isForbidden())
        	.andExpect(status().reason(testResponse)); 

	}

	@Test
	public void testURIMatchSinglePermissionMatch() throws Exception {
		
        String transactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";
        String testUrl = "/single/permission/required";
        String testResponse = "Response from MockRestService";

        mockServer
        	.expect(requestTo(primaryServiceBaseUrl + testUrl))
        	.andExpect(method(HttpMethod.GET))
        	.andExpect(header(transactionIdHeaderName, transactionId))
        	.andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));
        
        // Send request to mock server with transaction Id
        mockMvc
        	.perform(MockMvcRequestBuilders.get(testUrl).accept(MediaType.APPLICATION_JSON).header(transactionIdHeaderName, transactionId))
        	.andExpect(status().isOk())
            .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();        
        
	}
	
	@Test
	public void testURIPUTMatchSinglePermissionMatch() throws Exception {
		
        String transactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";
        String testUrl = "/single/permission/required";
        String testResponse = "Response from MockRestService";

        mockServer
        	.expect(requestTo(primaryServiceBaseUrl + testUrl))
        	.andExpect(method(HttpMethod.PUT))
        	.andExpect(header(transactionIdHeaderName, transactionId))
        	.andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));
        
        // Send request to mock server with transaction Id
        mockMvc
        	.perform(MockMvcRequestBuilders.put(testUrl).accept(MediaType.APPLICATION_JSON).header(transactionIdHeaderName, transactionId))
        	.andExpect(status().isOk())
            .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();        
        
	}
	
	
	@Test
	public void testURIPATCHMatchSinglePermissionMatch() throws Exception {
		
        String transactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";
        String testUrl = "/single/permission/required";
        String testResponse = "Sorry, the request is not allowed";
        
        // Send request to mock server with transaction Id
        mockMvc
        	.perform(MockMvcRequestBuilders.patch(testUrl).accept(MediaType.APPLICATION_JSON).header(transactionIdHeaderName, transactionId))
        	.andExpect(status().isForbidden())
        	.andExpect(status().reason(testResponse));        

        mockServer.verify();        
        
	}	
	
	@Test
	public void testURIMatchMultiplePermissionMatch() throws Exception {
		
        String transactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";
        String testUrl = "/multiple/permissions/required";
        String testResponse = "Response from MockRestService";

        mockServer
        	.expect(requestTo(primaryServiceBaseUrl + testUrl))
        	.andExpect(method(HttpMethod.GET))
        	.andExpect(header(transactionIdHeaderName, transactionId))
        	.andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));
        
        // Send request to mock server with transaction Id
        mockMvc
        	.perform(MockMvcRequestBuilders.get(testUrl).accept(MediaType.APPLICATION_JSON).header(transactionIdHeaderName, transactionId))
        	.andExpect(status().isOk())
            .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();        
        
	}
	
	@Test
	public void testURIMatchMultipleMissingOnePermissionMatch() throws Exception {
		
        String testUrl = "/multiple/permissions/required/one/missing";
        String testResponse = "Sorry, the request is not allowed";
        
        mockMvc
        	.perform(get(testUrl))
        	.andExpect(status().isForbidden())
        	.andExpect(status().reason(testResponse));         
	}	
	
	@Test
	public void testURIInstanceActionWildCardPermissionMatch() throws Exception {
		
        String transactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";
        String testUrl = "/wildcard/permission/granted";
        String testResponse = "Response from MockRestService";

        mockServer
        	.expect(requestTo(primaryServiceBaseUrl + testUrl))
        	.andExpect(method(HttpMethod.GET))
        	.andExpect(header(transactionIdHeaderName, transactionId))
        	.andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));
        
        // Send request to mock server with transaction Id
        mockMvc
        	.perform(MockMvcRequestBuilders
        				.get(testUrl)
        				.accept(MediaType.APPLICATION_JSON)
        				.header(transactionIdHeaderName, transactionId)
        				.header("PermissionsUser", "UserWithInstanceActionWildcardPermissionGranted")
        			)
        	.andExpect(status().isOk())
            .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();        
        
	}
	
	@Test
	public void testURIInstanceWildCardPermissionMatch() throws Exception {
		
        String transactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";
        String testUrl = "/instance/wildcard/permission/granted";
        String testResponse = "Response from MockRestService";

        mockServer
        	.expect(requestTo(primaryServiceBaseUrl + testUrl))
        	.andExpect(method(HttpMethod.GET))
        	.andExpect(header(transactionIdHeaderName, transactionId))
        	.andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));
        
        // Send request to mock server with transaction Id
        mockMvc
        	.perform(MockMvcRequestBuilders
        				.get(testUrl)
        				.accept(MediaType.APPLICATION_JSON)
        				.header(transactionIdHeaderName, transactionId)
        				.header("PermissionsUser", "UserWithInstanceWildcardPermissionGranted")
        			)
        	.andExpect(status().isOk())
            .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();        
        
	}
	
	@Test
	public void testURIActionWildCardPermissionMatch() throws Exception {
		
        String transactionId = "63f88b50-6345-4a61-bc59-3a48cabb60a4";
        String testUrl = "/action/wildcard/permission/granted";
        String testResponse = "Response from MockRestService";

        mockServer
        	.expect(requestTo(primaryServiceBaseUrl + testUrl))
        	.andExpect(method(HttpMethod.GET))
        	.andExpect(header(transactionIdHeaderName, transactionId))
        	.andRespond(withSuccess(testResponse, MediaType.APPLICATION_JSON));
        
        // Send request to mock server with transaction Id
        mockMvc
        	.perform(MockMvcRequestBuilders
        				.get(testUrl)
        				.accept(MediaType.APPLICATION_JSON)
        				.header(transactionIdHeaderName, transactionId)
        				.header("PermissionsUser", "UserWithActionWildcardPermissionGranted")
        			)
        	.andExpect(status().isOk())
            .andExpect(content().string(equalTo(testResponse)));

        mockServer.verify();        
        
	}	

}
