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
package org.onap.aaf.cadi.sidecar.fproxy;

import java.util.HashMap;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.eclipse.jetty.util.security.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@SpringBootApplication
@PropertySource("file:${CONFIG_HOME}/fproxy.properties")
public class FProxyApplication extends SpringBootServletInitializer {

    @Autowired
    private Environment env;

    @FunctionalInterface
    public interface AppProperty {
        String getProperty(String p);
    }

    /**
     * Spring Boot initialization.
     *
     * @param args main args
     */
    public static void main(String[] args) {
        AppProperty appProp = (String propertyName) -> Optional.ofNullable(System.getProperty(propertyName))
                .orElseThrow(() -> new IllegalArgumentException("Env property " + propertyName + " not set"));

        HashMap<String, Object> props = new HashMap<>();
        props.put("server.ssl.key-store-password", Password.deobfuscate(appProp.getProperty("KEY_STORE_PASSWORD")));
        props.put("server.ssl.trust-store-password", Password.deobfuscate(appProp.getProperty("TRUST_STORE_PASSWORD")));
        new FProxyApplication().configure(new SpringApplicationBuilder(FProxyApplication.class).properties(props))
                .run(args);
    }

    /**
     * Set required trust and key store system properties using values from application.properties
     */
    @PostConstruct
    public void setSystemProperties() {
        AppProperty appProp = (String propertyName) -> Optional.ofNullable(env.getProperty(propertyName))
                .orElseThrow(() -> new IllegalArgumentException("Env property " + propertyName + " not set"));

        System.setProperty("javax.net.ssl.keyStore", appProp.getProperty("server.ssl.key-store"));
        System.setProperty("javax.net.ssl.keyStorePassword", appProp.getProperty("server.ssl.key-store-password"));
        System.setProperty("javax.net.ssl.trustStore", appProp.getProperty("server.ssl.trust-store"));
        System.setProperty("javax.net.ssl.trustStorePassword", appProp.getProperty("server.ssl.trust-store-password"));
    }

}
