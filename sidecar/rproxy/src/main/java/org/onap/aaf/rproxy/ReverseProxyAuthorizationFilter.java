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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.cadi.CadiWrap;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.rproxy.config.ReverseProxyURIAuthorizationProperties;
import org.onap.aaf.rproxy.utils.ReverseProxyAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@EnableConfigurationProperties(ReverseProxyURIAuthorizationProperties.class)
public class ReverseProxyAuthorizationFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReverseProxyAuthorizationFilter.class);

    private List<ReverseProxyAuthorization> reverseProxyAuthorizations = new ArrayList<>();

    @Resource
    private ReverseProxyURIAuthorizationProperties reverseProxyURIAuthorizationProperties;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        // Read in the URI Authorisation configuration file
        String authFilePath = reverseProxyURIAuthorizationProperties.getConfigurationFile();
        if (authFilePath != null) {
            try (InputStream inputStream =
                    new FileInputStream(new File(reverseProxyURIAuthorizationProperties.getConfigurationFile()));
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream))) {
                List<ReverseProxyAuthorization> untrimmedList = new Gson().fromJson(jsonReader,
                        new TypeToken<ArrayList<ReverseProxyAuthorization>>() {}.getType());
                untrimmedList.removeAll(Collections.singleton(null));
                reverseProxyAuthorizations = untrimmedList;
            } catch (IOException e) {
                throw new ServletException("Authorizations config file not found.", e);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        CadiWrap cadiWrap = (CadiWrap) servletRequest;
        Principal principal = cadiWrap.getUserPrincipal();
        List<Permission> grantedPermissions = new ArrayList<>();
        cadiWrap.getLur().fishAll(principal, grantedPermissions);

        if (LOGGER.isDebugEnabled()) {
            logNeededPermissions();
        }

        String requestPath;
        try {
            requestPath = new URI(((HttpServletRequest) servletRequest).getRequestURI()).getPath();
        } catch (URISyntaxException e) {
            throw new ServletException("Request URI not valid", e);
        }

        if (authorizeRequest(grantedPermissions, requestPath)) {
            LOGGER.info("Authorized");
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            LOGGER.info("Unauthorized");
            ((HttpServletResponse) servletResponse).setStatus(HttpStatus.FORBIDDEN_403);
            ((HttpServletResponse) servletResponse).setContentType("application/json");
            ((HttpServletResponse) servletResponse).sendError(HttpStatus.FORBIDDEN_403,
                    "Sorry, the request is not allowed");
        }
    }

    /**
     * Check if the granted permissions for the request path matches the configured needed permissions.
     * 
     * @param grantedPermissions The granted permissions for the request path
     * @param requestPath The request path
     * @return true if permissions match
     */
    private boolean authorizeRequest(List<Permission> grantedPermissions, String requestPath) {
        boolean authorized = false;
        for (ReverseProxyAuthorization reverseProxyAuthorization : reverseProxyAuthorizations) {
            if (requestPath.matches(reverseProxyAuthorization.getUri())) {
                LOGGER.debug("The URI:{}  matches:{}", requestPath, reverseProxyAuthorization.getUri());
                if (checkPermissionsMatch(grantedPermissions, reverseProxyAuthorization)) {
                    authorized = true;
                    break;
                }
            } else {
                LOGGER.debug("The URI:{} doesn't match any in the configuration:{}", requestPath,
                        reverseProxyAuthorization.getUri());
            }
        }
        return authorized;
    }

    /**
     * Check all needed permissions match the granted permissions.
     * 
     * @param grantedPermissions the granted permissions
     * @param reverseProxyAuthorization the bean that contains the needed permissions
     * @return true if all needed permissions match
     */
    private boolean checkPermissionsMatch(List<Permission> grantedPermissions,
            ReverseProxyAuthorization reverseProxyAuthorization) {

        boolean matchedAllPermissions = true;
        for (String neededPermission : reverseProxyAuthorization.getPermissions()) {

            // Check needed permission is granted
            boolean matchedNeededPermission = false;
            for (Permission grantedPermission : grantedPermissions) {
                if (checkGrantedPermission(neededPermission, grantedPermission.getKey())) {
                    LOGGER.debug("Permission match found - needed permission:{}, granted permission:{}",
                            neededPermission, grantedPermission.getKey());
                    matchedNeededPermission = true;
                    break;
                }
            }
            if (!matchedNeededPermission) {
                matchedAllPermissions = false;
                break;
            }
        }
        return matchedAllPermissions;
    }

    /**
     * Check whether an AAF style permission matches a needed permission. Wildcards (*) are supported.
     * 
     * @param neededPermission, the needed permission
     * @param grantedPermission, the granted permission
     * 
     * @return true if the needed permission matches a granted permission
     */
    private boolean checkGrantedPermission(String neededPermission, String grantedPermission) {
        boolean permissionMatch = false;
        if (grantedPermission.matches(neededPermission)) {
            permissionMatch = true;
        } else if (grantedPermission.contains("*")) {
            String[] splitNeededPermission = neededPermission.split("\\\\\\|");
            String[] splitGrantedPermission = grantedPermission.split("\\|");
            if ((splitGrantedPermission[0].matches(splitNeededPermission[0]))
                    && (splitGrantedPermission[1].equals("*")
                            || splitGrantedPermission[1].matches(splitNeededPermission[1]))
                    && (splitGrantedPermission[2].equals("*")
                            || splitGrantedPermission[2].matches(splitNeededPermission[2]))) {
                permissionMatch = true;
            }
        }
        return permissionMatch;
    }

    /**
     * Log the needed permissions for each URL configured.
     */
    private void logNeededPermissions() {
        for (ReverseProxyAuthorization reverseProxyAuthorization : reverseProxyAuthorizations) {
            LOGGER.debug("URI For authorization: {}", reverseProxyAuthorization.getUri());
            for (String permission : reverseProxyAuthorization.getPermissions()) {
                LOGGER.debug("\t Needed permission:{}", permission);
            }
        }
    }

    @Override
    public void destroy() {
        // No op
    }
}
