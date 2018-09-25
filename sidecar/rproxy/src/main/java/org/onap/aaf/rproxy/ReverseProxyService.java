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
package org.onap.aaf.rproxy;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.UUID;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.onap.aaf.fproxy.data.CredentialCacheData;
import org.onap.aaf.fproxy.data.CredentialCacheData.CredentialType;
import org.onap.aaf.rproxy.config.ForwardProxyProperties;
import org.onap.aaf.rproxy.config.PrimaryServiceProperties;
import org.onap.aaf.rproxy.logging.ReverseProxyMethodLogTimeAnnotation;
import org.onap.aaf.rproxy.utils.ReverseProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@EnableConfigurationProperties({ForwardProxyProperties.class, PrimaryServiceProperties.class})
public class ReverseProxyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseProxyService.class);

    private String validatedTransactionId;

    @Resource(name = "ForwardProxyProperties")
    private ForwardProxyProperties forwardProxyProperties;

    @Resource(name = "PrimaryServiceProperties")
    private PrimaryServiceProperties primaryServiceProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${transactionid.header.name}")
    private String transactionIdHeader;

    @RequestMapping("/**")
    @ReverseProxyMethodLogTimeAnnotation
    public ResponseEntity<String> handleRequest(HttpServletRequest request,
            @RequestHeader(value = "${transactionid.header.name}", defaultValue = "") String transactionId,
            @RequestBody(required = false) String requestBody, HttpMethod requestMethod) throws URISyntaxException {
        validatedTransactionId = getValidTransactionId(transactionId);

        // Extract Request Permissions and store in Forward Proxy cache
        CredentialCacheData credentialCacheData = getCredentialDataFromRequest(request);
        if (credentialCacheData != null) {
            postCredentialsToCache(credentialCacheData);
        }

        // Call out to Primary Service & Return Response
        URI requestURI = new URI(request.getRequestURI());

        LOGGER.debug("Request URI: {}", request.getRequestURI());

        // Get Request Endpoint & substitute in local values
        URI primaryServiceURI = new URI(primaryServiceProperties.getProtocol(), requestURI.getUserInfo(),
                primaryServiceProperties.getHost(), Integer.parseInt(primaryServiceProperties.getPort()),
                requestURI.getPath(), requestURI.getQuery(), requestURI.getFragment());

        LOGGER.debug("Primary Service URI:{}, HTTP Method: {}", primaryServiceURI, requestMethod);

        HttpHeaders requestHeaders = setForwardedRequestHeaders(request);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, requestHeaders);

        return restTemplate.exchange(primaryServiceURI, requestMethod, httpEntity, String.class);
    }

    private String getValidTransactionId(String transactionId) {
        LOGGER.debug("Request transaction ID: {}", transactionId);
        if (transactionId == null || !ReverseProxyUtils.validTransactionId(transactionId)) {
            transactionId = UUID.randomUUID().toString();
        }
        LOGGER.debug("Validated transaction ID: {}", transactionId);
        return transactionId;
    }

    private HttpHeaders setForwardedRequestHeaders(HttpServletRequest httpServletRequest) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equals(transactionIdHeader)) {
                httpHeaders.set(headerName, httpServletRequest.getHeader(headerName));
            }
        }
        // Always set transaction ID
        httpHeaders.set(transactionIdHeader, validatedTransactionId);

        return httpHeaders;
    }

    /**
     * Retrieves credential data from request.
     * 
     * @param request The request to retrieve credentials from
     * @return The retrieved credential data, or null if no credentials are found in request
     */
    private CredentialCacheData getCredentialDataFromRequest(HttpServletRequest request) {
        CredentialCacheData credentialCacheData = null;
        String authValue = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authValue != null) {
            credentialCacheData = new CredentialCacheData(HttpHeaders.AUTHORIZATION, authValue, CredentialType.HEADER);
        }
        return credentialCacheData;
    }

    /**
     * Posts credential data to credential cache endpoint
     * 
     * @param credentialCacheData The credential data to post
     * @throws URISyntaxException
     */
    private void postCredentialsToCache(CredentialCacheData credentialCacheData) throws URISyntaxException {
        URI forwardProxyURI = new URI(forwardProxyProperties.getProtocol(), null, forwardProxyProperties.getHost(),
                forwardProxyProperties.getPort(), forwardProxyProperties.getCacheurl() + "/" + validatedTransactionId,
                null, null);

        ResponseEntity<String> response =
                restTemplate.postForEntity(forwardProxyURI, credentialCacheData, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new InvalidEndpointRequestException("Error posting to credential cache.",
                    "Status code: " + response.getStatusCodeValue() + " Message: " + response.getBody());
        }
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": Forward proxy host:" + forwardProxyProperties.getHost()
                + ": Primary service host:" + primaryServiceProperties.getHost();

    }
}
