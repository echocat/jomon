/*****************************************************************************************
 * *** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 2.0
 *
 * echocat Jomon, Copyright (c) 2012 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * *** END LICENSE BLOCK *****
 ****************************************************************************************/

package org.echocat.jomon.net;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class FreeTcpPortDetector {
    
    private final Random _random = new Random();
    private final InetAddress _address;
    private final int _minPort;
    private final int _maxPort;
    private final int _maxTries;

    public FreeTcpPortDetector(@Nonnull InetAddress address, @Nonnegative int minPort, @Nonnegative int maxPort) {
        if (minPort > maxPort) {
            throw new IllegalArgumentException("The maxPort have to larger than or equal to the minPort.");
        }
        _address = address;
        _minPort = minPort;
        _maxPort = maxPort;
        _maxTries = (maxPort - minPort) + 1;
    }

    @Nonnegative
    public int detect() throws NoSuchElementException {
        final Set<Integer> alreadyTriedPorts = new HashSet<>();
        int tries = 0;
        Integer port = null;
        while (port == null && tries < _maxTries) {
            final int range = _maxPort - _minPort;
            final int currentPort = range > 0 ? _random.nextInt(_maxPort - _minPort) + _minPort : _minPort;
            if (!alreadyTriedPorts.contains(currentPort)) {
                tries++;
                alreadyTriedPorts.add(currentPort);
                try {
                    new Socket(_address, currentPort).close();
                } catch (ConnectException ignored) {
                    port = currentPort;
                } catch (IOException e) {
                    throw new RuntimeException("Could not find a free port on '" + _address + "' between ports " + _minPort + " and " + _maxPort + ".", e);
                }
            }
        }
        if (port == null) {
            throw new NoSuchElementException("Could not find a free port on '" + _address + "' between ports " + _minPort + " and " + _maxPort + ".");
        }
        return port;
    }
}
