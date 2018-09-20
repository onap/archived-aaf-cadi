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
package org.onap.aaf.rproxy.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ReverseProxyEntryExitLoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseProxyEntryExitLoggingAspect.class);

    @Before("execution(* org.onap.platform.security.*.*(..))")
    public void before(JoinPoint joinPoint) {
        LOGGER.info("Entry of {}#{}", joinPoint.getTarget().getClass(), joinPoint.getSignature().getName());
    }

    @After("execution(* org.onap.platform.security.*.*(..))")
    public void after(JoinPoint joinPoint) {
        LOGGER.info("Exit of {}#{}", joinPoint.getTarget().getClass(), joinPoint.getSignature().getName());
    }
}
