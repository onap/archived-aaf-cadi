#!/bin/sh

# ============LICENSE_START=======================================================
# org.onap.aaf
# ================================================================================
# Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
# Copyright © 2017-2018 European Software Marketing Ltd.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================

BASEDIR="/opt/app/fproxy"

if [ -z "${KEY_STORE_PASSWORD}" ]; then
	echo "KEY_STORE_PASSWORD must be set in order to start up process"
	exit 1
fi

if [ -z "${TRUST_STORE_PASSWORD}" ]; then
	echo "TRUST_STORE_PASSWORD must be set in order to start up process"
	exit 1
fi

PROPS="-DKEY_STORE_PASSWORD=${KEY_STORE_PASSWORD}"
PROPS="$PROPS -DTRUST_STORE_PASSWORD=${TRUST_STORE_PASSWORD}"

JVM_MAX_HEAP=${MAX_HEAP:-1024}

exec java -Xmx${JVM_MAX_HEAP}m ${PROPS} -jar ${BASEDIR}/fproxy-exec.jar
