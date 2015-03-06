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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.*;
import java.util.*;

import static java.lang.Thread.currentThread;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * @author Brian Wellington &lt;bwelling@xbill.org&gt;
 */

@SuppressWarnings({"ContinueStatement", "OverlyLongMethod", "MethodWithMultipleReturnPoints", "AssignmentToMethodParameter", "InfiniteLoopStatement", "StatementWithEmptyBody"})
public class DnsServer implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DnsServer.class);

    private volatile boolean _closed;
    private final Set<Thread> _threads = new HashSet<>();
    private final Set<Closeable> _closeables = new HashSet<>();

    static final int FLAG_DNSSECOK = 1;
    static final int FLAG_SIGONLY = 2;

    private final Map<Integer, Cache> _caches;
    private final Map<Name, Zone> _znames;
    private final Map<Name, TSIG> _tsigs;

    private static String addrport(InetAddress addr, int port) {
        return addr.getHostAddress() + "#" + port;
    }

    public DnsServer() {
        this(null);
    }

    public DnsServer(@Nullable String config) {
        final BufferedReader reader = new BufferedReader(new StringReader(config));
        _caches = new HashMap<>();
        _znames = new HashMap<>();
        _tsigs = new HashMap<>();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                final StringTokenizer st = new StringTokenizer(line);
                if (!st.hasMoreTokens()) {
                    continue;
                }
                final String keyword = st.nextToken();
                if (!st.hasMoreTokens()) {
                    throw new IllegalArgumentException("Invalid line: " + line);
                }
                if (keyword.charAt(0) == '#') {
                    continue;
                }
                if (keyword.equals("primary")) {
                    addPrimaryZone(st.nextToken(), st.nextToken());
                } else if (keyword.equals("secondary")) {
                    addSecondaryZone(st.nextToken(), st.nextToken());
                } else if (keyword.equals("cache")) {
                    final Cache cache = new Cache(st.nextToken());
                    _caches.put(DClass.IN, cache);
                } else if (keyword.equals("key")) {
                    final String s1 = st.nextToken();
                    final String s2 = st.nextToken();
                    if (st.hasMoreTokens()) {
                        addTSIG(s1, s2, st.nextToken());
                    } else {
                        addTSIG("hmac-md5", s1, s2);
                    }
                } else {
                    throw new IllegalArgumentException("unknown keyword: " + keyword);
                }
            }
        } catch (IOException | ZoneTransferException e) {
            throw new RuntimeException("Could not create server.", e);
        }
        LOG.info("jnamed: running");
    }

    public void addPrimaryZone(String zname, String zonefile) throws IOException {
        Name origin = null;
        if (zname != null) {
            origin = Name.fromString(zname, Name.root);
        }
        final Zone newzone = new Zone(origin, zonefile);
        addPrimaryZone(newzone);
    }

    public void addPrimaryZone(@Nonnull Zone zone) throws IOException {
        _znames.put(zone.getOrigin(), zone);
    }

    public void addSecondaryZone(String zone, String remote)
        throws IOException, ZoneTransferException {
        final Name zname = Name.fromString(zone, Name.root);
        final Zone newzone = new Zone(zname, DClass.IN, remote);
        _znames.put(zname, newzone);
    }

    public void addSecondaryZone(@Nonnull Zone zone) {
        _znames.put(zone.getOrigin(), zone);
    }

    public void addTSIG(String algstr, String namestr, String key) throws IOException {
        final Name name = Name.fromString(namestr, Name.root);
        _tsigs.put(name, new TSIG(algstr, namestr, key));
    }

    public Cache getCache(int dclass) {
        Cache c = _caches.get(dclass);
        if (c == null) {
            c = new Cache(dclass);
            _caches.put(dclass, c);
        }
        return c;
    }

    public Zone findBestZone(Name name) {
        Zone foundzone;
        foundzone = _znames.get(name);
        if (foundzone != null) {
            return foundzone;
        }
        final int labels = name.labels();
        for (int i = 1; i < labels; i++) {
            final Name tname = new Name(name, i);
            foundzone = _znames.get(tname);
            if (foundzone != null) {
                return foundzone;
            }
        }
        return null;
    }

    public RRset findExactMatch(Name name, int type, int dclass, boolean glue) {
        final Zone zone = findBestZone(name);
        if (zone != null) {
            return zone.findExactMatch(name, type);
        } else {
            final RRset[] rrsets;
            final Cache cache = getCache(dclass);
            if (glue) {
                rrsets = cache.findAnyRecords(name, type);
            } else {
                rrsets = cache.findRecords(name, type);
            }
            if (rrsets == null) {
                return null;
            } else {
                return rrsets[0]; /* not quite right */
            }
        }
    }

    void addRRset(Name name, Message response, RRset rrset, int section, int flags) {
        for (int s = 1; s <= section; s++) {
            if (response.findRRset(name, rrset.getType(), s)) {
                return;
            }
        }
        if ((flags & FLAG_SIGONLY) == 0) {
            final Iterator<?> it = rrset.rrs();
            while (it.hasNext()) {
                Record r = (Record) it.next();
                if (r.getName().isWild() && !name.isWild()) {
                    r = r.withName(name);
                }
                response.addRecord(r, section);
            }
        }
        if ((flags & (FLAG_SIGONLY | FLAG_DNSSECOK)) != 0) {
            final Iterator<?> it = rrset.sigs();
            while (it.hasNext()) {
                Record r = (Record) it.next();
                if (r.getName().isWild() && !name.isWild()) {
                    r = r.withName(name);
                }
                response.addRecord(r, section);
            }
        }
    }

    private void addSOA(Message response, Zone zone) {
        response.addRecord(zone.getSOA(), Section.AUTHORITY);
    }

    private void addNS(Message response, Zone zone, int flags) {
        final RRset nsRecords = zone.getNS();
        addRRset(nsRecords.getName(), response, nsRecords,
            Section.AUTHORITY, flags);
    }

    private void addCacheNS(Message response, Cache cache, Name name) {
        final SetResponse sr = cache.lookupRecords(name, Type.NS, Credibility.HINT);
        if (!sr.isDelegation()) {
            return;
        }
        final RRset nsRecords = sr.getNS();
        final Iterator<?> it = nsRecords.rrs();
        while (it.hasNext()) {
            final Record r = (Record) it.next();
            response.addRecord(r, Section.AUTHORITY);
        }
    }

    private void addGlue(Message response, Name name, int flags) {
        final RRset a = findExactMatch(name, Type.A, DClass.IN, true);
        if (a == null) {
            return;
        }
        addRRset(name, response, a, Section.ADDITIONAL, flags);
    }

    private void addAdditional2(Message response, int section, int flags) {
        final Record[] records = response.getSectionArray(section);
        for (int i = 0; i < records.length; i++) {
            final Record r = records[i];
            final Name glueName = r.getAdditionalName();
            if (glueName != null) {
                addGlue(response, glueName, flags);
            }
        }
    }

    private void addAdditional(Message response, int flags) {
        addAdditional2(response, Section.ANSWER, flags);
        addAdditional2(response, Section.AUTHORITY, flags);
    }

    byte addAnswer(Message response, Name name, int type, int dclass,
              int iterations, int flags) {
        final SetResponse sr;
        byte rcode = Rcode.NOERROR;

        if (iterations > 6) {
            return Rcode.NOERROR;
        }

        if (type == Type.SIG || type == Type.RRSIG) {
            type = Type.ANY;
            flags |= FLAG_SIGONLY;
        }

        final Zone zone = findBestZone(name);
        if (zone != null) {
            sr = zone.findRecords(name, type);
        } else {
            final Cache cache = getCache(dclass);
            sr = cache.lookupRecords(name, type, Credibility.NORMAL);
        }

        if (sr.isUnknown()) {
            addCacheNS(response, getCache(dclass), name);
        }
        if (sr.isNXDOMAIN()) {
            response.getHeader().setRcode(Rcode.NXDOMAIN);
            if (zone != null) {
                addSOA(response, zone);
                if (iterations == 0) {
                    response.getHeader().setFlag(Flags.AA);
                }
            }
            rcode = Rcode.NXDOMAIN;
        } else if (sr.isNXRRSET()) {
            if (zone != null) {
                addSOA(response, zone);
                if (iterations == 0) {
                    response.getHeader().setFlag(Flags.AA);
                }
            }
        } else if (sr.isDelegation()) {
            final RRset nsRecords = sr.getNS();
            addRRset(nsRecords.getName(), response, nsRecords,
                Section.AUTHORITY, flags);
        } else if (sr.isCNAME()) {
            final CNAMERecord cname = sr.getCNAME();
            final RRset rrset = new RRset(cname);
            addRRset(name, response, rrset, Section.ANSWER, flags);
            if (zone != null && iterations == 0) {
                response.getHeader().setFlag(Flags.AA);
            }
            rcode = addAnswer(response, cname.getTarget(),
                type, dclass, iterations + 1, flags);
        } else if (sr.isDNAME()) {
            final DNAMERecord dname = sr.getDNAME();
            RRset rrset = new RRset(dname);
            addRRset(name, response, rrset, Section.ANSWER, flags);
            final Name newname;
            try {
                newname = name.fromDNAME(dname);
            } catch (final NameTooLongException ignored) {
                return Rcode.YXDOMAIN;
            }
            rrset = new RRset(new CNAMERecord(name, dclass, 0, newname));
            addRRset(name, response, rrset, Section.ANSWER, flags);
            if (zone != null && iterations == 0) {
                response.getHeader().setFlag(Flags.AA);
            }
            rcode = addAnswer(response, newname, type, dclass,
                iterations + 1, flags);
        } else if (sr.isSuccessful()) {
            final RRset[] rrsets = sr.answers();
            for (int i = 0; i < rrsets.length; i++) {
                addRRset(name, response, rrsets[i],
                    Section.ANSWER, flags);
            }
            if (zone != null) {
                addNS(response, zone, flags);
                if (iterations == 0) {
                    response.getHeader().setFlag(Flags.AA);
                }
            } else {
                addCacheNS(response, getCache(dclass), name);
            }
        }
        return rcode;
    }

    byte[] doAXFR(Name name, Message query, TSIG tsig, TSIGRecord qtsig, Socket s) {
        final Zone zone = _znames.get(name);
        boolean first = true;
        if (zone == null) {
            return errorMessage(query, Rcode.REFUSED);
        }
        final Iterator<?> it = zone.AXFR();
        try {
            final DataOutputStream dataOut;
            dataOut = new DataOutputStream(s.getOutputStream());
            final int id = query.getHeader().getID();
            while (it.hasNext()) {
                final RRset rrset = (RRset) it.next();
                final Message response = new Message(id);
                final Header header = response.getHeader();
                header.setFlag(Flags.QR);
                header.setFlag(Flags.AA);
                addRRset(rrset.getName(), response, rrset,
                    Section.ANSWER, FLAG_DNSSECOK);
                if (tsig != null) {
                    tsig.applyStream(response, qtsig, first);
                    qtsig = response.getTSIG();
                }
                first = false;
                final byte[] out = response.toWire();
                dataOut.writeShort(out.length);
                dataOut.write(out);
            }
        } catch (final IOException ignored) {
            LOG.info("AXFR failed");
        }
        closeQuietly(s);
        return null;
    }

    /*
    * Note: a null return value means that the caller doesn't need to do
    * anything.  Currently this only happens if this is an AXFR request over
    * TCP.
    */
    byte[] generateReply(Message query, byte[] in, int length, Socket s)
        throws IOException {
        final Header header;
        final int maxLength;
        int flags = 0;

        header = query.getHeader();
        if (header.getFlag(Flags.QR)) {
            return null;
        }
        if (header.getRcode() != Rcode.NOERROR) {
            return errorMessage(query, Rcode.FORMERR);
        }
        if (header.getOpcode() != Opcode.QUERY) {
            return errorMessage(query, Rcode.NOTIMP);
        }

        final Record queryRecord = query.getQuestion();

        final TSIGRecord queryTSIG = query.getTSIG();
        TSIG tsig = null;
        if (queryTSIG != null) {
            tsig = _tsigs.get(queryTSIG.getName());
            if (tsig == null ||
                tsig.verify(query, in, length, null) != Rcode.NOERROR) {
                return formerrMessage(in);
            }
        }

        final OPTRecord queryOPT = query.getOPT();
        if (queryOPT != null && queryOPT.getVersion() > 0) {
        }

        if (s != null) {
            maxLength = 65535;
        } else if (queryOPT != null) {
            maxLength = Math.max(queryOPT.getPayloadSize(), 512);
        } else {
            maxLength = 512;
        }

        if (queryOPT != null && (queryOPT.getFlags() & ExtendedFlags.DO) != 0) {
            flags = FLAG_DNSSECOK;
        }

        final Message response = new Message(query.getHeader().getID());
        response.getHeader().setFlag(Flags.QR);
        if (query.getHeader().getFlag(Flags.RD)) {
            response.getHeader().setFlag(Flags.RD);
        }
        response.addRecord(queryRecord, Section.QUESTION);

        final Name name = queryRecord.getName();
        final int type = queryRecord.getType();
        final int dclass = queryRecord.getDClass();
        if (type == Type.AXFR && s != null) {
            return doAXFR(name, query, tsig, queryTSIG, s);
        }
        if (!Type.isRR(type) && type != Type.ANY) {
            return errorMessage(query, Rcode.NOTIMP);
        }

        final byte rcode = addAnswer(response, name, type, dclass, 0, flags);
        if (rcode != Rcode.NOERROR && rcode != Rcode.NXDOMAIN) {
            return errorMessage(query, rcode);
        }

        addAdditional(response, flags);

        if (queryOPT != null) {
            final int optflags = (flags == FLAG_DNSSECOK) ? ExtendedFlags.DO : 0;
            final OPTRecord opt = new OPTRecord((short) 4096, rcode, (byte) 0,
                optflags);
            response.addRecord(opt, Section.ADDITIONAL);
        }

        response.setTSIG(tsig, Rcode.NOERROR, queryTSIG);
        return response.toWire(maxLength);
    }

    byte[] buildErrorMessage(Header header, int rcode, Record question) {
        final Message response = new Message();
        response.setHeader(header);
        for (int i = 0; i < 4; i++) {
            response.removeAllRecords(i);
        }
        if (rcode == Rcode.SERVFAIL) {
            response.addRecord(question, Section.QUESTION);
        }
        header.setRcode(rcode);
        return response.toWire();
    }

    public byte[] formerrMessage(byte[] in) {
        final Header header;
        try {
            header = new Header(in);
        } catch (final IOException ignored) {
            return null;
        }
        return buildErrorMessage(header, Rcode.FORMERR, null);
    }

    public byte[] errorMessage(Message query, int rcode) {
        return buildErrorMessage(query.getHeader(), rcode, query.getQuestion());
    }

    public void TCPclient(Socket s) {
        try {
            final int inLength;
            final DataInputStream dataIn;
            final DataOutputStream dataOut;
            final byte[] in;

            final InputStream is = s.getInputStream();
            dataIn = new DataInputStream(is);
            inLength = dataIn.readUnsignedShort();
            in = new byte[inLength];
            dataIn.readFully(in);

            final Message query;
            byte[] response;
            try {
                query = new Message(in);
                response = generateReply(query, in, in.length, s);
                if (response == null) {
                    return;
                }
            } catch (final IOException ignored) {
                response = formerrMessage(in);
            }
            dataOut = new DataOutputStream(s.getOutputStream());
            dataOut.writeShort(response.length);
            dataOut.write(response);
        } catch (final IOException e) {
            LOG.warn("TCPclient(" + addrport(s.getLocalAddress(), s.getLocalPort()) + ").", e);
        } finally {
            try {
                s.close();
            } catch (final IOException ignored) {}
        }
    }

    public void serveTCP(InetSocketAddress address) {
        try {
            final ServerSocket sock = new ServerSocket(address.getPort(), 128, address.getAddress());
            synchronized (_closeables) {
                _closeables.add(sock);
            }
            while (!currentThread().isInterrupted()) {
                final Socket s = accept(sock);
                final Thread thread;
                thread = new Thread(new Runnable() { @Override public void run() {
                    TCPclient(s);
                }});
                _threads.add(thread);
                thread.start();
            }
        } catch (final InterruptedIOException ignored) {
            currentThread().interrupt();
        } catch (final IOException e) {
            LOG.warn("serveTCP(" + addrport(address.getAddress(), address.getPort()) + ")", e);
        }
    }

    @Nonnull
    private Socket accept(@Nonnull ServerSocket sock) throws IOException {
        try {
            return sock.accept();
        } catch (final SocketException e) {
            if (sock.isClosed()) {
                final InterruptedIOException toThrow = new InterruptedIOException();
                toThrow.initCause(e);
                throw toThrow;
            } else {
                throw e;
            }
        }
    }

    public void serveUDP(InetSocketAddress address) {
        try {
            final DatagramSocket sock = new DatagramSocket(address.getPort(), address.getAddress());
            synchronized (_closeables) {
                _closeables.add(sock);
            }
            final short udpLength = 512;
            final byte[] in = new byte[udpLength];
            final DatagramPacket indp = new DatagramPacket(in, in.length);
            DatagramPacket outdp = null;
            while (!currentThread().isInterrupted()) {
                indp.setLength(in.length);
                receive(sock, indp);
                final Message query;
                byte[] response;
                try {
                    query = new Message(in);
                    response = generateReply(query, in,
                        indp.getLength(),
                        null);
                    if (response == null) {
                        continue;
                    }
                } catch (final IOException ignored) {
                    response = formerrMessage(in);
                }
                if (outdp == null) {
                    outdp = new DatagramPacket(response,
                        response.length,
                        indp.getAddress(),
                        indp.getPort());
                } else {
                    outdp.setData(response);
                    outdp.setLength(response.length);
                    outdp.setAddress(indp.getAddress());
                    outdp.setPort(indp.getPort());
                }
                sock.send(outdp);
            }
        } catch (final InterruptedIOException ignored) {
            currentThread().interrupt();
        } catch (final IOException e) {
            LOG.warn("serveUDP(" + addrport(address.getAddress(), address.getPort()) + ")", e);
        }
    }

    private static void receive(@Nonnull DatagramSocket sock, @Nonnull DatagramPacket indp) throws IOException {
        try {
            sock.receive(indp);
        } catch (final SocketException e) {
            if (sock.isClosed()) {
                final InterruptedIOException toThrow = new InterruptedIOException();
                toThrow.initCause(e);
                throw toThrow;
            } else {
                throw e;
            }
        }
    }

    public void addTCP(final InetSocketAddress address) {
        synchronized (this) {
            assertNotClosed();
            final Thread t;
            t = new Thread(new Runnable() { @Override public void run() {
                serveTCP(address);
            }});
            _threads.add(t);
            t.start();
        }
    }

    public void addUDP(final InetSocketAddress address) {
        synchronized (this) {
            assertNotClosed();
            final Thread t;
            t = new Thread(new Runnable() { @Override public void run() {
                serveUDP(address);
            }});
            _threads.add(t);
            t.start();
        }
    }

    protected void assertNotClosed() {
        if (_closed) {
            throw new IllegalStateException("This server was already closed.");
        }
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            _closed = true;
            synchronized (_closeables) {
                for (final Closeable closeable : _closeables) {
                    closeQuietly(closeable);
                }
            }
            for (final Thread thread : _threads) {
                do {
                    thread.interrupt();
                    try {
                        thread.join(10);
                    } catch (final InterruptedException ignored) {
                        LOG.info("Got interrupted and could not wait for end of '" + thread + "'.");
                        currentThread().interrupt();
                    }
                } while (!currentThread().isInterrupted() && thread.isAlive());
            }
        }
    }
}
