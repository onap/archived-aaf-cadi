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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.jetty.util.security.Password;
import org.onap.aaf.cadi.filter.CadiFilter;
import org.onap.aaf.rproxy.config.ForwardProxyProperties;
import org.onap.aaf.rproxy.config.PrimaryServiceProperties;
import org.onap.aaf.rproxy.config.ReverseProxySSLProperties;
import org.onap.aaf.rproxy.mocks.ReverseProxyMockCadiFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.RegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ServletComponentScan
@EnableConfigurationProperties(ReverseProxySSLProperties.class)
@PropertySource("file:${CONFIG_HOME}/reverse-proxy.properties")
public class ReverseProxyApplication extends SpringBootServletInitializer {

    private static final String CADI_TRUSTSTORE_PASS = "cadi_truststore_password";

    @Autowired
    private Environment env;

    /**
     * Spring Boot Initialisation.
     * 
     * @param args main args
     */
    public static void main(String[] args) {
        String keyStorePassword = System.getProperty("KEY_STORE_PASSWORD");
        if (keyStorePassword == null || keyStorePassword.isEmpty()) {
            throw new IllegalArgumentException("Env property KEY_STORE_PASSWORD not set");
        }
        HashMap<String, Object> props = new HashMap<>();
        props.put("server.ssl.key-store-password", Password.deobfuscate(keyStorePassword));
        new ReverseProxyApplication()
                .configure(new SpringApplicationBuilder(ReverseProxyApplication.class).properties(props)).run(args);
    }

    /**
     * Set required trust store system properties using values from application.properties
     */
    @PostConstruct
    public void setSystemProperties() {
        String keyStorePath = env.getProperty("server.ssl.key-store");
        if (keyStorePath != null) {
            String keyStorePassword = env.getProperty("server.ssl.key-store-password");

            if (keyStorePassword != null) {
                System.setProperty("javax.net.ssl.keyStore", keyStorePath);
                System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
                System.setProperty("javax.net.ssl.trustStore", keyStorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
            } else {
                throw new IllegalArgumentException("Env property server.ssl.key-store-password not set");
            }
        }
    }

    @Resource
    private ReverseProxySSLProperties reverseProxySSLProperties;

    @Resource
    Properties cadiProps;

    @Bean(name = "ForwardProxyProperties")
    public ForwardProxyProperties forwardProxyProperties() {
        return new ForwardProxyProperties();
    }

    @Bean(name = "PrimaryServiceProperties")
    public PrimaryServiceProperties primaryServiceProperties() {
        return new PrimaryServiceProperties();
    }

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
                .loadKeyMaterial(ResourceUtils.getFile(reverseProxySSLProperties.getClientcert()),
                        reverseProxySSLProperties.getKeystorePassword().toCharArray(),
                        reverseProxySSLProperties.getKeystorePassword().toCharArray())
                .loadTrustMaterial(ResourceUtils.getFile(reverseProxySSLProperties.getKeystore()),
                        reverseProxySSLProperties.getKeystorePassword().toCharArray())
                .build();

        return HttpClients.custom().setSSLContext(sslContext);
    }

    @Profile("cadi")
    @Bean
    public FilterRegistrationBean<CadiFilter> registerCADIFilter() {

        FilterRegistrationBean<CadiFilter> filterRegistrationBean = new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new CadiFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setName("CADIFilter");
        filterRegistrationBean.setOrder(RegistrationBean.HIGHEST_PRECEDENCE);

        // Deobfuscate truststore password
        String trustStorePassword = cadiProps.getProperty(CADI_TRUSTSTORE_PASS);
        if (trustStorePassword != null) {
            cadiProps.setProperty(CADI_TRUSTSTORE_PASS, Password.deobfuscate(trustStorePassword));
        }

        // Add filter init params
        cadiProps.forEach((k, v) -> filterRegistrationBean.addInitParameter((String) k, (String) v));

        return filterRegistrationBean;
    }

    @Profile("mockCadi")
    @Bean
    public FilterRegistrationBean<ReverseProxyMockCadiFilter> registerMockCADIFilter() {

        FilterRegistrationBean<ReverseProxyMockCadiFilter> filterRegistrationBean = new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new ReverseProxyMockCadiFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setName("CADIFilter");
        filterRegistrationBean.setOrder(RegistrationBean.HIGHEST_PRECEDENCE);

        return filterRegistrationBean;
    }
}
