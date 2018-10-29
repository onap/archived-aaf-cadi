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
package org.onap.aaf.fproxy;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.jetty.util.security.Password;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${server.ssl.client-cert}")
    private String clientCertPath;

    @Value("${server.ssl.client-cert-password}")
    private String clientCertPassword;

    @Value("${server.ssl.key-store}")
    private String keystorePath;

    @Value("${server.ssl.key-store-password}")
    private String keystorePassword;

    @Profile("secure")
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws GeneralSecurityException, IOException {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(getClientBuilder().build()));
    }

    @Profile("noHostVerification")
    @Bean
    public RestTemplate restTemplateNoHostVerification(RestTemplateBuilder builder)
            throws GeneralSecurityException, IOException {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(
                getClientBuilder().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build()));
    }

    private HttpClientBuilder getClientBuilder() throws GeneralSecurityException, IOException {

        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(ResourceUtils.getFile(clientCertPath), Password.deobfuscate(clientCertPassword).toCharArray(),
                        keystorePassword.toCharArray())
                .loadTrustMaterial(ResourceUtils.getFile(keystorePath), keystorePassword.toCharArray()).build();

        return HttpClients.custom().setSSLContext(sslContext);
    }
}
