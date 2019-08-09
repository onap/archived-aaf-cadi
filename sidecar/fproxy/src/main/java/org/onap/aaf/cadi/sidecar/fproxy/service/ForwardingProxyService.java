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
package org.onap.aaf.cadi.sidecar.fproxy.service;

import java.net.URI;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.sidecar.fproxy.cache.utils.CacheUtils;
import org.onap.aaf.cadi.sidecar.fproxy.data.CredentialCacheData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class ForwardingProxyService {

    Logger logger = LoggerFactory.getLogger(ForwardingProxyService.class);

    private static final long DEFAULT_CACHE_EXPIRY_MS = 180000; // 3 mins

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CacheUtils cacheUtils;

    @Value("${credential.cache.timeout.ms:" + DEFAULT_CACHE_EXPIRY_MS + "}")
    long cacheExpiryMs;

    @RequestMapping(value = "/credential-cache/{transactionId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addCredentialToCache(@PathVariable("transactionId") String transactionId,
            @RequestBody CredentialCacheData credentialdata) {
        logger.info("Updating credential cache with transaction ID: {}", transactionId);

        // Update credential cache
        logger.debug("Credential data: {}", credentialdata);
        cacheUtils.addCredentialsToCache(transactionId, credentialdata, cacheExpiryMs);

        logger.info("Credential cache successfully updated with transaction ID: {}", transactionId);
        return new ResponseEntity<>(transactionId, HttpStatus.OK);
    }

    @RequestMapping("/**")
    public ResponseEntity<String> forwardRest(@RequestBody(required = false) String body, HttpMethod method,
            HttpServletRequest request, HttpServletResponse response) {

        String requestUrl = request.getRequestURI();

        logger.info("Request received: {}", requestUrl);

        URI uri = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString()).query(request.getQueryString())
                .build(true).toUri();

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        cacheUtils.populateCredentialsFromCache(headers, request);

        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        logger.info("Forwarding request...");

        return restTemplate.exchange(uri, method, httpEntity, String.class);
    }
}
