# Forward Proxy Introduction

The **AAF Forward Proxy** (or **fProxy**) is a forward proxy service with credential caching capabilities for incoming REST requests. It is one of the two applications (alongside with [Reverse proxy][1]) deployed as a
Kubernetes sidecar to the main Primary service.

## Features

**Forward Proxy**:

* The service will forward all incoming REST requests onto their original endpoints.
* Add any cached security credentials to the forwarding request

### Credential Cache:
The credential cache is a short-lived in-memory cache, keyed on a transaction ID. The following data is cached:

* `Transaction ID` - this is the key for retrieving cached values
* `CredentialName` - this is the name of the credential to be cached.
          This should correspond to the header name for a header credential, or the cookie name for a cookie credential.
* `CredentialValue` - this is the value associated with the credential.
          This should correspond to the header value of a header credential, or the cookie contents for a cookie credential.
* `CredentialType` - this is the type of the credential to be cached. Currently supported values are: HEADER, COOKIE.
          The cache has a configurable cache expiry period, so that any cache entries older than the expiry period will be automatically removed from the cache.
		  
### Credential Cache REST API:
Credentials can be added to the credential cache by performing a REST POST using the following URL:

(Note that the transaction ID is provided as a URL parameter)

`https://<host>:<port>/credential-cache/<transactionid>`
The body of the request should contain the cached data (described above) in JSON format as follows:

```
{ "credentialName":"foo", "credentialValue":"bar", "credentialType":"<HEADER/COOKIE>" }
```

## Configuring the fProxy service
The **fProxy service** is configured through the `fproxy.properties` file that resides under the `${CONFIG_HOME}` environment variable.

The file has the following configurable properties:

- `credential.cache.timeout.ms`	This is the time in milliseconds that a cache entry will expire after it is added. e.g. 180000
- `transactionid.header.name`	This is the name of the header in incoming requests that will contain the transaction ID. e.g. X-TransactionId


[1]: ../rproxy/README.md
