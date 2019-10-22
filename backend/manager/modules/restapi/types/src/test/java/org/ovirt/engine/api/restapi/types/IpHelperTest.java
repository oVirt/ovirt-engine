/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.IpVersion;

public class IpHelperTest {
    /**
     * Checks that when given a {@code null} IP configuration object it returns {@code null}.
     */
    @Test
    public void testReturnNullWhenGivenNullIp() {
        IpVersion version = IpHelper.getVersion(null);
        assertNull(version);
    }

    /**
     * Checks that when explicitly given IPv4 it ignores the address.
     */
    @Test
    public void testIgnoreAddressWhenV4ExplicitlyGiven() {
        Ip ip = new Ip();
        ip.setAddress("::1");
        ip.setVersion(IpVersion.V4);
        IpVersion version = IpHelper.getVersion(ip);
        assertEquals(IpVersion.V4, version);
    }

    /**
     * Checks that when explicitly given IPv6 it ignores the address.
     */
    @Test
    public void testIgnoreAddressWhenV6ExplicitlyGiven() {
        Ip ip = new Ip();
        ip.setAddress("127.0.0.1");
        ip.setVersion(IpVersion.V6);
        IpVersion version = IpHelper.getVersion(ip);
        assertEquals(IpVersion.V6, version);
    }

    /**
     * Checks that when the version isn't given explicitly, and the address is a valid IPv4 address, the returned
     * version is IPv4.
     */
    @Test
    public void testReturnsV4WhenGivenValidV4Address() {
        Ip ip = new Ip();
        ip.setAddress("127.0.0.1");
        IpVersion version = IpHelper.getVersion(ip);
        assertEquals(IpVersion.V4, version);
    }

    /**
     * Checks that when the version isn't given explicitly, and the address is a valid IPv6 address, the returned
     * version is IPv6.
     */
    @Test
    public void testReturnsV6WhenGivenValidV6Address() {
        Ip ip = new Ip();
        ip.setAddress("::1");
        IpVersion version = IpHelper.getVersion(ip);
        assertEquals(IpVersion.V6, version);
    }
}
