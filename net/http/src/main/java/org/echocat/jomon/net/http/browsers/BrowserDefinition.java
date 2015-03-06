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

package org.echocat.jomon.net.http.browsers;

import com.google.common.collect.Sets;
import org.echocat.jomon.runtime.util.Glob;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@ThreadSafe
public class BrowserDefinition {

    private static final Set<String> KNOWN_TABLET_DEVICES = Sets.newHashSet("ipad", "playbook", "sch-i800", "gt-p1000", "xoom");
    
    private final String _userAgent;
    private final Map<String, String> _properties;
    private final String _parent;
    private final Glob _glob;

    public BrowserDefinition(@Nonnull String userAgent, @Nonnull Map<String, String> properties, @Nullable String parent) {
        _userAgent = userAgent;
        _properties = properties;
        _parent = parent;
        try {
            _glob = new Glob(userAgent, true);
        } catch (final ParseException e) {
            throw new IllegalArgumentException("Could not parse the userAgent as glob pattern: " + userAgent, e);
        }
    }

    public Map<String, String> getProperties() {
        return unmodifiableMap(_properties);
    }

    public String getBrowserName() {
        return getPropertyValue("Browser");
    }

    public String getDeviceName() {
        return getPropertyValue("Device_Name");
    }
    
    public String getPlatformVersion() {
        return getPropertyValue("Platform_Version");
    }

    public String getVersion() {
        final String result;
        final String version = getPropertyValue("Version");
        // In case no version is set use major/minor version as fallback
        if ("0".equals(version)) {
            result = getMajorVersion() + "." + getMinorVersion();
        } else {
            result = version;
        }
        return result;
    }

    public String getMajorVersion() {
        return getPropertyValue("MajorVer");
    }

    public String getMinorVersion() {
        return getPropertyValue("MinorVer");
    }

    public String getPlatform() {
        return getPropertyValue("Platform");
    }

    public String getCssVersion() {
        return getPropertyValue("CSSVersion");
    }

    public boolean isFrameEnabled() {
        return isPropertyTrue("Frames");
    }

    public boolean isIFrameEnabled() {
        return isPropertyTrue("IFrames");
    }

    /**
     * Means that the browser is *technically* able to handle cookies. This
     * does _not_ mean that the user accepts cookies.
     */
    public boolean isCookieEnabled() {
        return isPropertyTrue("Cookies");
    }

    public boolean isVbScriptEnabled() {
        return isPropertyTrue("VBScript");
    }

    public boolean isJavaScriptEnabled() {
        return isPropertyTrue("JavaScript");
    }

    public boolean isJavaAppletsEnabled() {
        return isPropertyTrue("JavaApplets");
    }

    public boolean isActiveXControlsEnabled() {
        return isPropertyTrue("ActiveXControls");
    }

    public boolean isCdfEnabled() {
        return isPropertyTrue("CDF");
    }

    public boolean isAol() {
        return isPropertyTrue("AOL");
    }

    public boolean isCrawler() {
        return isPropertyTrue("Crawler");
    }

    public boolean isStripper() {
        return isPropertyTrue("Stripper");
    }

    public boolean isWapDevice() {
        return isPropertyTrue("WAP");
    }

    public boolean isMobileDevice() {
        final boolean result;
        if (isPropertyTrue("isMobileDevice")) {
            result = true;
        } else {
            final String browserName = getBrowserName();
            result = browserName != null && browserName.toLowerCase().contains("mobile") && isCrawler();
        }
        return result;
    }

    public boolean isTablet() {
        final boolean result;
        if (isMobileDevice()) {
            final String deviceName = getDeviceName();
            result = deviceName != null && KNOWN_TABLET_DEVICES.contains(deviceName.toLowerCase());
        } else {
            result = false;
        }
        return result;
    }
    
    public boolean isParent() {
        return _userAgent.equals(_parent);
    }

    public String getParent() {
        return _parent;
    }

    public String getUserAgent() {
        return _userAgent;
    }

    Glob getGlob() {
        return _glob;
    }

    private String getPropertyValue(String name) {
        return _properties.get(name);
    }

    private boolean isPropertyTrue(String name) {
        final String p = getPropertyValue(name);
        return p != null && p.equals("true");
    }

    public String toExternalForm() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getBrowserName());
        if (isNotEmpty(getPlatformVersion()) && !"0".equals(getPlatformVersion())) {
            sb.append(' ').append(getPlatformVersion());
        } else {
            if (isNotEmpty(getMajorVersion())) {
                sb.append(' ').append(getMajorVersion());
            }
            if (isNotEmpty(getMinorVersion())) {
                if (isEmpty(getMajorVersion())) {
                    sb.append(' ');
                }
                sb.append('.').append(getMinorVersion());
            }
        }
        if (isNotEmpty(getPlatform())) {
            sb.append(" on ").append(getPlatform());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toExternalForm();
    }
}
