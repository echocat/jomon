/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2013 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.net.http.client;

import org.echocat.jomon.runtime.util.Duration;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.http.client.protocol.ClientContext.AUTH_CACHE;
import static org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
import static org.apache.http.params.HttpConnectionParams.setSoReuseaddr;

public class HttpClientFactory {

    private HttpHost _proxy;
    private ClientConnectionManager _connectionManager = new PoolingClientConnectionManager();
    private boolean _acceptAllCertificates;
    private Iterable<HttpClientAuth> _auths;
    private Duration _connectionTimeout = new Duration("5s");
    private Duration _soTimeout = new Duration("1m");
    private boolean _soReuseAddress;

    public HttpHost getProxy() {
        return _proxy;
    }

    public void setProxy(HttpHost proxy) {
        _proxy = proxy;
    }

    public void setProxyUri(URI uri) {
        if (uri != null) {
            _proxy = HttpClientUtils.toHttpHost(uri);
        } else {
            _proxy = null;
        }
    }

    public ClientConnectionManager getConnectionManager() {
        return _connectionManager;
    }

    public void setConnectionManager(ClientConnectionManager connectionManager) {
        _connectionManager = connectionManager;
    }

    public boolean isAcceptAllCertificates() {
        return _acceptAllCertificates;
    }

    public void setAcceptAllCertificates(boolean acceptAllCertificates) {
        _acceptAllCertificates = acceptAllCertificates;
    }

    public Iterable<HttpClientAuth> getAuths() {
        return _auths;
    }

    public void setAuths(Iterable<HttpClientAuth> auths) {
        _auths = auths;
    }

    public Duration getConnectionTimeout() {
        return _connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        _connectionTimeout = connectionTimeout;
    }

    public Duration getSoTimeout() {
        return _soTimeout;
    }

    public void setSoTimeout(Duration soTimeout) {
        _soTimeout = soTimeout;
    }

    public boolean isSoReuseAddress() {
        return _soReuseAddress;
    }

    public void setSoReuseAddress(boolean soReuseAddress) {
        _soReuseAddress = soReuseAddress;
    }

    @Nonnull
    public HttpClient create() throws Exception {
        final DefaultHttpClientImpl client = new DefaultHttpClientImpl(_connectionManager, null);
        final HttpHost proxy = _proxy;
        if (proxy != null) {
            configureProxy(client, proxy);
        }
        if (_acceptAllCertificates) {
            acceptAllCertificatesOf(client);
        }
        configureAuth(client, _auths);
        configureTimes(client, _connectionTimeout, _soTimeout, _soReuseAddress);


        return client;
    }

    protected void configureProxy(@Nonnull HttpClient client, @Nonnull HttpHost proxy) {
        client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }

    protected void acceptAllCertificatesOf(@Nonnull HttpClient client) throws Exception {
        final SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{new X509TrustManagerImpl()}, null);
        final SSLSocketFactory ssf = new SSLSocketFactory(ctx, ALLOW_ALL_HOSTNAME_VERIFIER);
        client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, ssf));
    }

    protected void configureAuth(@Nonnull DefaultHttpClientImpl client, @Nullable Iterable<HttpClientAuth> scopeAndCredentials) {
        final AuthCache authCache = new BasicAuthCache();
        if (scopeAndCredentials != null) {
            for (HttpClientAuth auth : scopeAndCredentials) {
                final AuthScope authScope = auth.getScope();
                client.getCredentialsProvider().setCredentials(authScope, auth.getCredentials());
                authCache.put(auth.getHost(), auth.getScheme());
            }
        }
        client.setAuthCache(authCache);
    }

    private void configureTimes(@Nonnull HttpClient client, @Nonnull Duration connectionTimeout, @Nonnull Duration soTimeout, boolean soReuseAddress) {
        final HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, (int) connectionTimeout.in(MILLISECONDS));
        HttpConnectionParams.setSoTimeout(params, (int) soTimeout.in(MILLISECONDS));
        setSoReuseaddr(params, soReuseAddress);
    }


    protected class X509TrustManagerImpl implements X509TrustManager {
        @Override public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
        @Override public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {}
        @Override public X509Certificate[] getAcceptedIssuers() { return null; }
    }

    protected class DefaultHttpClientImpl extends DefaultHttpClient {

        private AuthCache _authCache;

        public DefaultHttpClientImpl(@Nullable ClientConnectionManager conman, @Nullable HttpParams params) {
            super(conman, params);
        }

        public AuthCache getAuthCache() {
            return _authCache;
        }

        public void setAuthCache(AuthCache authCache) {
            _authCache = authCache;
        }

        @Override
        protected HttpContext createHttpContext() {
            final HttpContext context = super.createHttpContext();
            final AuthCache authCache = _authCache;
            if (authCache != null) {
                context.setAttribute(AUTH_CACHE, authCache);
            }
            return context;
        }
    }

}
