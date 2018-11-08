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
package org.onap.aaf.cadi.sidecar.fproxy.data;

public class CredentialCacheData {

    public enum CredentialType {
        HEADER, COOKIE;
    }

    private String credentialName;
    private String credentialValue;
    private CredentialType credentialType;

    public CredentialCacheData() {
        super();
    }

    public CredentialCacheData(String credentialName, String credentialValue, CredentialType credentialType) {
        super();
        this.credentialName = credentialName;
        this.credentialValue = credentialValue;
        this.credentialType = credentialType;
    }

    public String getCredentialName() {
        return credentialName;
    }

    public void setCredentialName(String credentialName) {
        this.credentialName = credentialName;
    }

    public String getCredentialValue() {
        return credentialValue;
    }

    public void setCredentialValue(String credentialValue) {
        this.credentialValue = credentialValue;
    }

    public Enum<CredentialType> getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(CredentialType credentialType) {
        this.credentialType = credentialType;
    }

    @Override
    public String toString() {
        return "CredentialCacheData [credentialName=" + credentialName + ", credentialType=" + credentialType + "]";
    }

}
