package org.ovirt.engine.core.utils.network.predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IpAddressPredicateTest {

    private static final String SOME_NON_IPV6_STRING = "some string";

    @Test
    public void testPositive() {
        assertTrue(new IpAddressPredicate("1::2").test("1:0:0:0:0:0:0:2"));
    }

    @Test
    public void testCompressedIpv6AndNotCompressedIpv6() {
        assertTrue(new IpAddressPredicate("::").test("0:0:0:0:0:0:0:0"));
    }

    @Test
    public void testIpv4AddressWithLeadingZero() {
        assertTrue(new IpAddressPredicate("1.2.3.4").test("1.2.3.04"));
    }

    @Test
    public void testIpv4AddressNegative() {
        assertFalse(new IpAddressPredicate("1.2.3.4").test("1.2.3.5"));
    }

    @Test
    public void testBothNulls() {
        assertTrue(new IpAddressPredicate(null).test(null));
    }

    @Test
    public void testNullAndIpv6() {
        assertFalse(new IpAddressPredicate(null).test("::"));
    }

    @Test
    public void testNullAndNotIpv6() {
        assertFalse(new IpAddressPredicate(null).test(SOME_NON_IPV6_STRING));
    }

    @Test
    public void testIpv6AndNotIpv6() {
        assertFalse(new IpAddressPredicate(null).test(SOME_NON_IPV6_STRING));
    }

    @Test
    public void testDifferentNotIpv6Strings() {
        assertFalse(new IpAddressPredicate(SOME_NON_IPV6_STRING).test("some other string"));
    }

    @Test
    public void testSameNotIpv6Strings() {
        assertTrue(new IpAddressPredicate(SOME_NON_IPV6_STRING).test(SOME_NON_IPV6_STRING));
    }
}
