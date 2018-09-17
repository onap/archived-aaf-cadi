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
package org.onap.aaf.fproxy.cache.utils;

import javax.servlet.http.HttpServletRequest;
import org.onap.aaf.fproxy.cache.CredentialCache;
import org.onap.aaf.fproxy.data.CredentialCacheData;
import org.onap.aaf.fproxy.data.CredentialCacheData.CredentialType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
public class CacheUtils {

    Logger logger = LoggerFactory.getLogger(CacheUtils.class);

    @Autowired
    private CredentialCache credentialCache;

    @Value("${transactionid.header.name}")
    private String transactionIdHeaderName;

    public void populateCredentialsFromCache(HttpHeaders headers, HttpServletRequest request) {
        String transactionId = headers.getFirst(transactionIdHeaderName);
        if (transactionId != null) {
            CredentialCacheData cacheData = credentialCache.get(transactionId);
            if (cacheData == null) {
                logger.info("Transaction ID {} not found in cache, skipping credential population...", transactionId);
            } else if (cacheData.getCredentialType().equals(CredentialType.HEADER)) {
                logger.info("Populating header credentials from cache for transaction ID: {}", transactionId);
                applyHeaderCacheData(cacheData, headers);
            } else if (cacheData.getCredentialType().equals(CredentialType.COOKIE)) {
                logger.info("Populating cookie credentials from cache for transaction ID: {}", transactionId);
                applyCookieCacheData(cacheData, headers, request);
            }
        } else {
            logger.info("No transaction ID found in request, skipping credential population...");
        }
    }

    private void applyHeaderCacheData(CredentialCacheData cacheData, HttpHeaders headers) {
        String credentialName = cacheData.getCredentialName();
        if (!headers.containsKey(credentialName)) {
            headers.add(credentialName, cacheData.getCredentialValue());
            logger.info("Header credentials successfully populated.");
        } else {
            logger.info("Request already contains header with name: {}, skipping credential population...",
                    credentialName);
        }
    }

    private void applyCookieCacheData(CredentialCacheData cacheData, HttpHeaders headers, HttpServletRequest request) {
        String credentialName = cacheData.getCredentialName();
        // Check if Cookie with same name is already set then skip
        if (WebUtils.getCookie(request, credentialName) == null) {
            headers.add(HttpHeaders.COOKIE, cacheData.getCredentialValue());
            logger.info("Cookie credentials successfully populated.");
        } else {
            logger.info("Request already contains cookie with name: {}, skipping credential population...",
                    credentialName);
        }
    }

    public void addCredentialsToCache(String transactionId, CredentialCacheData credentialdata, long cacheExpiryMs) {
        credentialCache.add(transactionId, credentialdata, cacheExpiryMs);
    }
}
