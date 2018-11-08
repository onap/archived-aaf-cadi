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
    
    /**
     * Spring Boot Initialization.
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
        new FProxyApplication().configure(new SpringApplicationBuilder(FProxyApplication.class).properties(props))
                .run(args);
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
}
