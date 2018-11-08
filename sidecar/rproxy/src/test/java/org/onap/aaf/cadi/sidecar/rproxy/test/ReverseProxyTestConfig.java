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

import org.onap.aaf.cadi.sidecar.rproxy.mocks.ReverseProxyMockCadiFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.RegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ReverseProxyTestConfig extends SpringBootServletInitializer {

    @Bean
    public FilterRegistrationBean<ReverseProxyMockCadiFilter> registerCADIFilter() {
        FilterRegistrationBean<ReverseProxyMockCadiFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new ReverseProxyMockCadiFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setName("CADIFilter");
        filterRegistrationBean.setOrder(RegistrationBean.HIGHEST_PRECEDENCE);

        return filterRegistrationBean;
    }
}
