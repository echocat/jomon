/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012-2014 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.net.dns;

import org.echocat.jomon.net.HostService;
import org.echocat.jomon.net.Protocol;
import org.echocat.jomon.net.dns.SrvDnsEntryEvaluator.NoSuchSrvRecordException;
import org.echocat.jomon.runtime.concurrent.RetryForSpecifiedCountStrategy;
import org.echocat.jomon.runtime.concurrent.RetryingStrategy;
import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;
import org.echocat.jomon.testing.environments.LogEnvironment;
import org.junit.Rule;
import org.junit.Test;
import org.xbill.DNS.SimpleResolver;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;

import static org.echocat.jomon.net.HostService.hostService;
import static org.echocat.jomon.net.Protocol.tcp;
import static org.echocat.jomon.net.dns.AddressUtils.toInetAddress;
import static org.echocat.jomon.net.dns.RecordUtils.a;
import static org.echocat.jomon.net.dns.RecordUtils.srv;
import static org.echocat.jomon.net.dns.ZoneUtils.zone;
import static org.echocat.jomon.runtime.concurrent.Retryer.executeWithRetry;
import static org.echocat.jomon.testing.BaseMatchers.is;
import static org.echocat.jomon.testing.IterableMatchers.containsAllItemsOf;
import static org.junit.Assert.assertThat;

public class SrvDnsEntryEvaluatorUnitTest {

    protected static final String DOMAIN = "my-domain.test.";
    protected static final String SERVICE3_HIST = "service3." + DOMAIN;
    protected static final String SERVICE2_HOST = "service2." + DOMAIN;
    protected static final String SERVICE1_HOST = "service1." + DOMAIN;

    protected static final String SERVICE_NAME = "my-service";
    protected static final RetryingStrategy<List<HostService>> RETRY_STRATEGY = RetryForSpecifiedCountStrategy.<List<HostService>>retryForSpecifiedCountOf(10).withExceptionsThatForceRetry(ServiceTemporaryUnavailableException.class).asUnmodifiable();

    @Rule
    public final LogEnvironment _logEnvironment = new LogEnvironment();
    @Rule
    public final TestDnsServer _dnsServer = new TestDnsServer();

    @Test
    public void testSuccessLookup() throws Exception {
        setupPrimaryZoneOf(_dnsServer);
        final List<HostService> services = lookup(SERVICE_NAME, tcp, DOMAIN);
        assertThat(services, containsAllItemsOf(
            hostService(toInetAddress(SERVICE1_HOST, "10.0.1.1"), 6001, 101, 11, 666),
            hostService(toInetAddress(SERVICE2_HOST, "10.0.1.2"), 6002, 102, 12, 666),
            hostService(toInetAddress(SERVICE3_HIST, "10.0.1.3"), 6003, 103, 13, 666)
        ));
    }

    @Test
    public void testUnknownServiceLookup() throws Exception {
        setupPrimaryZoneOf(_dnsServer);
        try {
            lookup("unknown-service", tcp, DOMAIN);
        } catch (final NoSuchSrvRecordException expected) {}
        try {
            lookup(SERVICE_NAME, tcp, "unknown-domain.test.");
        } catch (final UnknownHostException expected) {
            assertThat(expected instanceof NoSuchSrvRecordException, is(false));
        }
    }

    @Test
    public void testIllegalServerLookup() throws Exception {
        try {
            createEvaluator(new InetSocketAddress(InetAddress.getLocalHost(), 6666)).lookup(SERVICE_NAME, tcp, DOMAIN);
        } catch (final ServiceTemporaryUnavailableException expected) {}
    }

    protected static void setupPrimaryZoneOf(@Nonnull DnsServer dnsServer) throws IOException {
        dnsServer.addPrimaryZone(zone(DOMAIN, 666,
            a(SERVICE1_HOST, "10.0.1.1"),
            a(SERVICE2_HOST, "10.0.1.2"),
            a(SERVICE3_HIST, "10.0.1.3"),
            srv("_" + SERVICE_NAME + "._tcp." + DOMAIN, 666, 101, 11, 6001, SERVICE1_HOST),
            srv("_" + SERVICE_NAME + "._tcp." + DOMAIN, 666, 102, 12, 6002, SERVICE2_HOST),
            srv("_" + SERVICE_NAME + "._tcp." + DOMAIN, 666, 103, 13, 6003, SERVICE3_HIST)
        ));
    }

    @Nonnull
    protected List<HostService> lookup(@Nonnull final String service, @Nonnull final Protocol protocol, @Nonnull final String host) throws Exception {
        return executeWithRetry(new Callable<List<HostService>>() { @Override public List<HostService> call() throws Exception {
            return createEvaluator(_dnsServer.getAddress()).lookup(service, protocol, host);
        }}, RETRY_STRATEGY, Exception.class);
    }

    @Nonnull
    protected static SrvDnsEntryEvaluator createEvaluator(@Nonnull InetSocketAddress server) throws Exception {
        final SrvDnsEntryEvaluator evaluator = new SrvDnsEntryEvaluator();
        final SimpleResolver resolver = new SimpleResolver();
        resolver.setAddress(server);
        evaluator.setResolver(resolver);
        return evaluator;
    }

}
