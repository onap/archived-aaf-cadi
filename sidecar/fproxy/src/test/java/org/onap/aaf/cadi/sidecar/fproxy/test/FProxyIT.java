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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jetty.util.security.Password;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aaf.cadi.sidecar.fproxy.service.ForwardingProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FProxyIT {

    static {
        System.setProperty("server.ssl.key-store-password",
                Password.deobfuscate("OBF:1y0q1uvc1uum1uvg1pil1pjl1uuq1uvk1uuu1y10"));
    }

    @Autowired
    private ForwardingProxyService fProxyService;

    @Test
    public void contexLoads() throws Exception {
        assertThat(fProxyService).isNotNull();
    }
}
