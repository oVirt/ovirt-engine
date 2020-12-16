package org.ovirt.engine.core.sso.api.jwk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

// copied from nimbus-jose-jwt 8.2 and ported to junit5
// original author Vladimir Dzhuvinov
public class BigIntegerUtilsTest {

    @Test
    public void testBigIntegerLeadingZeroPadding() {

        byte[] a1 = new BigInteger("123456789A", 16).toByteArray();
        byte[] a2 = new BigInteger("F23456789A", 16).toByteArray();

        assertEquals(a1.length + 1, a2.length);
        assertEquals(0, a2[0]);
    }

    @Test
    public void testNoLeadingZero() {

        byte[] a1 = BigIntegerUtils.toBytesUnsigned(new BigInteger("123456789A", 16));
        byte[] a2 = BigIntegerUtils.toBytesUnsigned(new BigInteger("F23456789A", 16));

        assertEquals(a1.length, a2.length);
    }
}
