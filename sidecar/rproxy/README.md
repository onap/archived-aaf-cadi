# Introduction

The AAF Reverse Proxy is a proxy microservice which intercepts incoming REST requests by, extracting the credentials from the request and authenticate/authorises
with a configured security provider. It is one of two components (along with the Forward proxy) deployed as a Kubernetes sidecar to
separate the responsibility of authentication and authorization away from the primary microservice, this service is responsible for
controlling access to the REST URL endpoints exposed by the primary microservice, and propogating security credentials to downstream microservices. 

## Features

Reverse Proxy:

* The service will intercept all incoming REST requests to the primary service, extract and cache the token credentials in the Forward proxy.
* Invokes the authentication and authorisation providers to validate the extracted tokens, and retrieve its list of authorisations
* Invokes the enforcement point filter to determine whether the incoming request URI and retrieved permissions match the list of granted URIs and permissions
  configured in the URI authorisation file. If authorisation is successful, forward the request to the primary service.

## Configuring the rProxy service
The rProxy service is configured through property and json files that resides under the ${CONFIG_HOME} environment variable.

The files have the following configurable properties:

###cadi.properties

cadi_loglevel log level of the cadi filter, e.g. DEBUG, INFO
cadi_keyfile  location to the cadi key file
cadi_truststore 
cadi_truststore_password
aaf_url hostname and port of the server hosting the AAF service, e.g. https://aaf.osaaf.org:30247
aaf_env AAF environment type, e.g. DEV, PROD
aaf_id aafadmin user, e.g. demo@people.osaaf.org
aaf_password aafadmin user password encrypted with the cadi_keyfile, e.g. enc:92w4px0y_rrm265LXLpw58QnNPgDXykyA1YTrflbAKz
cadi_x509_issuers colon separated list of client cert issuers

###reverse-proxy.properties

transactionid.header.name	This is the name of the header in incoming requests that will contain the transaction ID.	X-TransactionId

###primary-service.properties

primary-service.protocol http protocol of the primary service e.g. https
primary-service.host location of the primary service, since this sidecar resides in the same pod of the primary service. localhost
primary-service.port port of the primary service

###forward-proxy.properties

forward-proxy.protocol http protocol of the fproxy service e.g. https
forward-proxy.host location of the fproxy service, since this sidecar resides in the same pod of the primary service. localhost
forward-proxy.port port of the fproxy service
forward-proxy.cacheurl URI to the store credential cache. /credential-cache

### auth/uri-authorization.json
This file contains the list of required AAF permissions authorised for the request URI, permissions will be tested against the first matching URI.
If the user doesn't have those permissions then the next matching URI will be tested until the list of URIs is exhausted.
URIs will be matched in order as positioned in the configuration file. Wildcarding is supported as standard regular expression matches for both URIs and permissions.

[
    {
      "uri": "URI 1",
      "permissions": [
        "permission 1",
        "permission 2",
        "..."]
    },
    {
      "uri": "URI 2",
      "permissions": [
        "permission 3",
        "permission 4",
        "..."]     
    }
]

e.g.
[
    {
      "uri": "\/aai\/v13\/cloud-infrastructure\/cloud-regions$",
      "permissions": [
        "org.onap.osaaf.resources.access|rest|read"
       ]
    },
    {
      "uri": "\/aai\/v13\/cloud-infrastructure\/cloud-regions\/cloud-region\/[^\/]+[\/][^\/]+$*",
      "permissions": [
        "org.onap.osaaf.resources.access|clouds|read",
        "org.onap.osaaf.auth.resources.access|tenants|read"
      ]     
    },
    {
      "uri": "\/aai\/v13\/cloud-infrastructure\/cloud-regions\/cloud-region\/[^\/]+[\/][^\/]+\/tenants/tenant/[^\/]+/vservers/vserver/[^\/]+$",
      "permissions": [
        "org.onap.osaaf.auth.resources.access|clouds|read",
        "org.onap.osaaf.auth.resources.access|tenants|read",
        "org.onap.osaaf.auth.resources.access|vservers|read"
      ]     
    }
]