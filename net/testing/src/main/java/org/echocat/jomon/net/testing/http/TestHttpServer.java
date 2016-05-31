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

package org.echocat.jomon.net.testing.http;

import org.echocat.jomon.net.FreeTcpPortDetector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class TestHttpServer implements Closeable, TestRule {

    private final Server _server;
    private final InetSocketAddress _address;

    public TestHttpServer() {
        try {
            _address = getFreeLocalHostSocketAddress();
            _server = createServer(_address);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create server.", e);
        }
    }
    
    public TestHttpServer(@Nonnull Handler handler) {
        this();
        try {
            setHandler(handler);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create server for " + handler + ".", e);
        }
    }

    public TestHttpServer(@Nonnull SimpleHandler handler) {
        this();
        try {
            setHandler(handler);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create server for " + handler + ".", e);
        }
    }

    public TestHttpServer(@Nonnull String contextPath, @Nonnull Servlet servlet) {
        this();
        try {
            setHandler(contextPath, servlet);
        } catch (final Exception e) {
            throw new RuntimeException("Could not create server for " + servlet + " at '" + contextPath + "'.", e);
        }
    }

    @Nonnull
    public Server getServer() {
        return _server;
    }

    @Nonnull
    public InetSocketAddress getAddress() {
        return _address;
    }

    public void setHandler(@Nonnull Handler handler) throws Exception {
        if (_server.isRunning()) {
            _server.stop();
        }
        _server.setHandler(handler);
        _server.start();
    }

    public void setHandler(@Nonnull final SimpleHandler handler) throws Exception {
        setHandler(new AbstractHandler() { @Override public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            // noinspection CaughtExceptionImmediatelyRethrown
            try {
                final String result = handler.handle(target, request, response);
                if (result != null) {
                    if (response.getContentType() == null) {
                        response.setContentType("text/plain; charset=UTF-8");
                    }
                    final PrintWriter writer = response.getWriter();
                    writer.write(result);
                    writer.flush();
                }
            } catch (IOException | ServletException e) {
                throw e;
            } catch (final Exception e) {
                throw new ServletException(e);
            }
            baseRequest.setHandled(true);
        }});
    }
    
    public void setHandler(@Nonnull final String contextPath, @Nonnull final Servlet servlet) throws Exception {
        final ServletContextHandler context = new ServletContextHandler();
        context.addServlet(new ServletHolder(servlet), contextPath);
        setHandler(context);
    }
    
    @Override
    public void close() {
        try {
            _server.stop();
        } catch (final Exception e) {
            throw new RuntimeException("Could not close " + _server + ".", e);
        }
    }

    @Nonnull
    public URI getBaseUri() {
        final StringBuilder sb = new StringBuilder();
        sb.append("http://").append(_address.getHostName());
        if (_address.getPort() != 80) {
            sb.append(':').append(_address.getPort());
        }
        return URI.create(sb.toString());
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() { @Override public void evaluate() throws Throwable {
            try {
                base.evaluate();
            } finally {
                try {
                    close();
                } catch (final Exception ignored) {}
            }
        }};
    }

    @Nonnull
    private static Server createServer(@Nonnull InetSocketAddress socketAddress) throws Exception {
        final Server server = new Server();
        server.addConnector(createConnector(socketAddress, server));
        return server;
    }

    @Nonnull
    private static ServerConnector createConnector(@Nonnull InetSocketAddress socketAddress, Server server) throws IOException {
        final ServerConnector connector = new ServerConnector(server);
        connector.setHost(socketAddress.getHostName());
        connector.setPort(socketAddress.getPort());
        return connector;
    }

    @Nonnull
    private static InetSocketAddress getFreeLocalHostSocketAddress() throws IOException {
        final InetAddress localHostAddress = getLocalHostAddress();
        final int port = getFreePort(localHostAddress);
        return InetSocketAddress.createUnresolved(localHostAddress.getHostName(), port);
    }
    
    @Nonnull
    private static InetAddress getLocalHostAddress() throws UnknownHostException {
        return InetAddress.getByName("localhost");
    }
    
    private static int getFreePort(@Nonnull InetAddress localHostAddress) throws IOException {
        final FreeTcpPortDetector detector = new FreeTcpPortDetector(localHostAddress, 10000, 45000);
        return detector.detect();
    }
    
    public interface SimpleHandler {
        public String handle(@Nonnull String path, @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) throws Exception;
    }
    
}
