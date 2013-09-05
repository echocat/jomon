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

package org.echocat.jomon.net.dns;

import org.echocat.jomon.net.HostService;
import org.echocat.jomon.net.Protocol;
import org.echocat.jomon.runtime.util.ServiceTemporaryUnavailableException;
import org.xbill.DNS.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.echocat.jomon.net.dns.ResolverUtils.toStringOfResolver;
import static org.xbill.DNS.Address.getByAddress;
import static org.xbill.DNS.Lookup.*;
import static org.xbill.DNS.Type.SRV;

@SuppressWarnings("DuplicateThrows")
public class SrvDnsEntryEvaluator {

    private Resolver _resolver;

    public SrvDnsEntryEvaluator() {}

    public SrvDnsEntryEvaluator(@Nullable Resolver resolver) {
        this();
        _resolver = resolver;
    }

    @Nonnull
    public String buildQueryFor(@Nonnull String service, @Nonnull Protocol protocol, @Nonnull String host) {
        return "_" + service + "._" + protocol.getName() + "." + host;
    }

    @Nonnull
    public List<HostService> lookup(@Nonnull String service, @Nonnull Protocol protocol, @Nonnull String host) throws NoSuchSrvRecordException, UnknownHostException, SocketException {
        return lookup(buildQueryFor(service, protocol, host));
    }

    @Nonnull
    public List<HostService> lookup(@Nonnull String query) throws NoSuchSrvRecordException, UnknownHostException, SocketException {
        final Lookup lookup = createLookupFor(query, SRV);
        lookup.run();
        checkResult(query, lookup);
        final SRVRecord[] records = getSrvRecordsOf(lookup);
        final List<HostService> result = new ArrayList<>();
        for (SRVRecord record : records) {
            final HostService service = toHostService(record);
            if (service != null) {
                result.add(service);
            }
        }
        return unmodifiableList(result);
    }

    @Nullable
    protected HostService toHostService(@Nonnull SRVRecord record) {
        final InetSocketAddress address = toInetSocketAddress(record);
        return address != null ? new HostService(address, record.getPriority(), record.getWeight(), record.getTTL()) : null;
    }

    @Nullable
    protected InetSocketAddress toInetSocketAddress(@Nonnull SRVRecord record) {
        final InetAddress address = toInetAddress(record);
        return address != null ? new InetSocketAddress(address, record.getPort()) : null;
    }

    @Nullable
    protected InetAddress toInetAddress(@Nonnull SRVRecord record) {
        final String name = record.getTarget().toString();
        InetAddress address;
        try {
            address = getByAddress(name);
        } catch (UnknownHostException ignored) {
            final Record [] records = createLookupFor(name, Type.A).run();
            if (records != null && records.length > 0) {
                address = toInetAddress(name, (ARecord) records[0]);
            } else {
                address = null;
            }
        }
        return address;
    }

    @Nonnull
    protected static InetAddress toInetAddress(@Nonnull String name, @Nonnull ARecord record) {
        try {
            return InetAddress.getByAddress(name, record.getAddress().getAddress());
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Could not convert " + record + " to address.", e);
        }
    }

    @Nonnull
    protected SRVRecord[] getSrvRecordsOf(@Nonnull Lookup lookup) {
        final Record[] records = lookup.getAnswers();
        if (records == null) {
            throw new IllegalStateException("Got no answers for " + lookup + " from remote " + toStringOfResolver(lookup) + ".");
        }
        final SRVRecord[] srvRecords = new SRVRecord[records.length];
        for (int i = 0; i < records.length; i++) {
            if (!(records[i] instanceof SRVRecord)) {
                throw new IllegalStateException("Got illegal answers for " + lookup + " from remote " + toStringOfResolver(lookup) + ".");
            }
            srvRecords[i] = (SRVRecord) records[i];
        }
        return srvRecords;
    }

    protected void checkResult(@Nonnull String query, @Nonnull Lookup lookup) throws NoSuchSrvRecordException, UnknownHostException, SocketException {
        final int result = lookup.getResult();
        if (result == HOST_NOT_FOUND) {
            throw new NoSuchSrvRecordException("Could not find srv entry for query '" + query + "' on remote " + toStringOfResolver(lookup) + ".");
        } else if (result == TYPE_NOT_FOUND) {
            throw new UnknownHostException("Could not find the host that should contain the srv entry for query '" + query + "' on remote " + toStringOfResolver(lookup) + ".");
        } else if (result == TRY_AGAIN) {
            throw new ServiceTemporaryUnavailableException("Could not get information from remote " + toStringOfResolver(lookup) + ".");
        } else if (result != 0) {
            throw new RuntimeException("Could not get information from remote " + toStringOfResolver(lookup) + ". Got: " + lookup.getErrorString());
        }
    }

    public void setResolver(Resolver resolver) {
        _resolver = resolver;
    }

    public void setResolverByHostName(final String resolverHostName) throws UnknownHostException {
        _resolver = resolverHostName != null ? new SimpleResolver(resolverHostName) : null;
    }

    @Nonnull
    protected Lookup createLookupFor(@Nonnull String query, @Nonnegative int type) {
        final Lookup lookup;
        try {
            lookup = new Lookup(query, type);
        } catch (TextParseException e) {
            throw new IllegalArgumentException("Could not parse query: " + query, e);
        }
        final Resolver resolver = _resolver;
        lookup.setResolver(resolver != null ? resolver : getDefaultResolver());
        lookup.setCache(null);
        return lookup;
    }

    public static class NoSuchSrvRecordException extends UnknownHostException {

        public NoSuchSrvRecordException(String message) {
            super(message);
        }
    }
}
