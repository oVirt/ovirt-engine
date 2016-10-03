/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.IpVersion;

public class IpHelperTest {
    /**
     * Checks that when given a {@code null} IP configuration object it returns {@code null}.
     */
    @Test
    public void testReturnNullWhenGivenNullIp() {
        IpVersion version = IpHelper.getVersion(null);
        assertEquals(null, version);
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
