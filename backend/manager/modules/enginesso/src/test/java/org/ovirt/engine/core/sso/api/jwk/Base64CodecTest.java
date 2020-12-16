package org.ovirt.engine.core.sso.api.jwk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

//copied from nimbus-jose-jwt 8.2  and ported to junit 5
public class Base64CodecTest {

    @Test
    public void testComputeEncodedLength() {

        assertEquals(0, Base64Codec.computeEncodedLength(0, false));
        assertEquals(4, Base64Codec.computeEncodedLength(1, false));
        assertEquals(4, Base64Codec.computeEncodedLength(2, false));
        assertEquals(4, Base64Codec.computeEncodedLength(3, false));
        assertEquals(8, Base64Codec.computeEncodedLength(4, false));
        assertEquals(8, Base64Codec.computeEncodedLength(5, false));
        assertEquals(8, Base64Codec.computeEncodedLength(6, false));

        assertEquals(0, Base64Codec.computeEncodedLength(0, true));
        assertEquals(2, Base64Codec.computeEncodedLength(1, true));
        assertEquals(3, Base64Codec.computeEncodedLength(2, true));
        assertEquals(4, Base64Codec.computeEncodedLength(3, true));
        assertEquals(6, Base64Codec.computeEncodedLength(4, true));
        assertEquals(7, Base64Codec.computeEncodedLength(5, true));
        assertEquals(8, Base64Codec.computeEncodedLength(6, true));
    }

    @Test
    public void testTpSelect() {

        assertEquals(Base64Codec.tpSelect(0, 43927, 50985034), 50985034);
        assertEquals(Base64Codec.tpSelect(1, 43927, 50985034), 43927);
        assertEquals(Base64Codec.tpSelect(0, -39248, 43298), 43298);
        assertEquals(Base64Codec.tpSelect(1, -98432, 96283), -98432);
        assertEquals(Base64Codec.tpSelect(0, -34, -12), -12);
        assertEquals(Base64Codec.tpSelect(1, -98, -11), -98);
    }

    @Test
    public void testTpLT() {

        assertEquals(Base64Codec.tpLT(23489, 0), 0);
        assertEquals(Base64Codec.tpLT(34, 9), 0);
        assertEquals(Base64Codec.tpLT(0, 9), 1);
        assertEquals(Base64Codec.tpLT(3, 9), 1);
        assertEquals(Base64Codec.tpLT(9, 9), 0);
        assertEquals(Base64Codec.tpLT(0, 0), 0);
        assertEquals(Base64Codec.tpLT(-23, -23), 0);
        assertEquals(Base64Codec.tpLT(-43, -23), 1);
        assertEquals(Base64Codec.tpLT(-43, 23), 1);
        assertEquals(Base64Codec.tpLT(43, -23), 0);
    }

    @Test
    public void testTpGT() {

        assertEquals(Base64Codec.tpGT(0, 23489), 0);
        assertEquals(Base64Codec.tpGT(9, 34), 0);
        assertEquals(Base64Codec.tpGT(9, 0), 1);
        assertEquals(Base64Codec.tpGT(9, 3), 1);
        assertEquals(Base64Codec.tpGT(9, 9), 0);
        assertEquals(Base64Codec.tpGT(0, 0), 0);
        assertEquals(Base64Codec.tpGT(-23, -23), 0);
        assertEquals(Base64Codec.tpGT(-23, -43), 1);
        assertEquals(Base64Codec.tpGT(23, -43), 1);
        assertEquals(Base64Codec.tpGT(-23, 43), 0);
    }

    @Test
    public void testTpEq() {

        assertEquals(Base64Codec.tpEq(0, 23489), 0);
        assertEquals(Base64Codec.tpEq(9, 34), 0);
        assertEquals(Base64Codec.tpEq(9, 0), 0);
        assertEquals(Base64Codec.tpEq(9, 3), 0);
        assertEquals(Base64Codec.tpEq(9, 9), 1);
        assertEquals(Base64Codec.tpEq(0, 0), 1);
        assertEquals(Base64Codec.tpEq(-23, -23), 1);
        assertEquals(Base64Codec.tpEq(-23, -43), 0);
        assertEquals(Base64Codec.tpEq(23, -43), 0);
        assertEquals(Base64Codec.tpEq(-23, 43), 0);
        assertEquals(Base64Codec.tpEq(0x7FFFFFFF, 0x7FFFFFFF), 1);
        assertEquals(Base64Codec.tpEq(0xFFFFFFFF, 0x7FFFFFFF), 0);
        assertEquals(Base64Codec.tpEq(0x7FFFFFFF, 0xFFFFFFFF), 0);
        assertEquals(Base64Codec.tpEq(0xFFFFFFFF, 0xFFFFFFFF), 1);
    }

    @Test
    public void testEncode() {

        assertEquals("YWE+", Base64Codec.encodeToString("aa>".getBytes(StandardCharsets.UTF_8), false));
        assertEquals("YmI/", Base64Codec.encodeToString("bb?".getBytes(StandardCharsets.UTF_8), false));

        // Test vectors from rfc4648#section-10
        assertEquals("", Base64Codec.encodeToString("".getBytes(StandardCharsets.UTF_8), false));
        assertEquals("Zg==", Base64Codec.encodeToString("f".getBytes(StandardCharsets.UTF_8), false));
        assertEquals("Zm8=", Base64Codec.encodeToString("fo".getBytes(StandardCharsets.UTF_8), false));
        assertEquals("Zm9v", Base64Codec.encodeToString("foo".getBytes(StandardCharsets.UTF_8), false));
        assertEquals("Zm9vYg==", Base64Codec.encodeToString("foob".getBytes(StandardCharsets.UTF_8), false));
        assertEquals("Zm9vYmE=", Base64Codec.encodeToString("fooba".getBytes(StandardCharsets.UTF_8), false));
        assertEquals("Zm9vYmFy", Base64Codec.encodeToString("foobar".getBytes(StandardCharsets.UTF_8), false));
    }

    @Test
    public void testEncodeUrlSafe() {

        assertEquals("YWE-", Base64Codec.encodeToString("aa>".getBytes(StandardCharsets.UTF_8), true));
        assertEquals("YmI_", Base64Codec.encodeToString("bb?".getBytes(StandardCharsets.UTF_8), true));

        // Test vectors from rfc4648#section-10 with stripped padding
        assertEquals("", Base64Codec.encodeToString("".getBytes(StandardCharsets.UTF_8), true));
        assertEquals("Zg", Base64Codec.encodeToString("f".getBytes(StandardCharsets.UTF_8), true));
        assertEquals("Zm8", Base64Codec.encodeToString("fo".getBytes(StandardCharsets.UTF_8), true));
        assertEquals("Zm9v", Base64Codec.encodeToString("foo".getBytes(StandardCharsets.UTF_8), true));
        assertEquals("Zm9vYg", Base64Codec.encodeToString("foob".getBytes(StandardCharsets.UTF_8), true));
        assertEquals("Zm9vYmE", Base64Codec.encodeToString("fooba".getBytes(StandardCharsets.UTF_8), true));
        assertEquals("Zm9vYmFy", Base64Codec.encodeToString("foobar".getBytes(StandardCharsets.UTF_8), true));
    }

    @Test
    public void testDecode() {

        assertEquals("aa>", new String(Base64Codec.decode("YWE+"), StandardCharsets.UTF_8));
        assertEquals("bb?", new String(Base64Codec.decode("YmI/"), StandardCharsets.UTF_8));

        assertEquals("", new String(Base64Codec.decode(""), StandardCharsets.UTF_8));
        assertEquals("f", new String(Base64Codec.decode("Zg=="), StandardCharsets.UTF_8));
        assertEquals("fo", new String(Base64Codec.decode("Zm8="), StandardCharsets.UTF_8));
        assertEquals("foo", new String(Base64Codec.decode("Zm9v"), StandardCharsets.UTF_8));
        assertEquals("foob", new String(Base64Codec.decode("Zm9vYg=="), StandardCharsets.UTF_8));
        assertEquals("fooba", new String(Base64Codec.decode("Zm9vYmE="), StandardCharsets.UTF_8));
        assertEquals("foobar", new String(Base64Codec.decode("Zm9vYmFy"), StandardCharsets.UTF_8));
    }

    @Test
    public void testDecodeWithIllegalChars() {

        assertEquals("", new String(Base64Codec.decode("\n"), StandardCharsets.UTF_8));
        assertEquals("f", new String(Base64Codec.decode("Zg==\n"), StandardCharsets.UTF_8));
        assertEquals("fo", new String(Base64Codec.decode("Zm8=\n"), StandardCharsets.UTF_8));
        assertEquals("foo", new String(Base64Codec.decode("Zm9v\n"), StandardCharsets.UTF_8));
        assertEquals("foob", new String(Base64Codec.decode("Zm9vYg==\n"), StandardCharsets.UTF_8));
        assertEquals("fooba", new String(Base64Codec.decode("Zm9vYmE=\n"), StandardCharsets.UTF_8));
        assertEquals("foobar", new String(Base64Codec.decode("Zm9vYmFy\n"), StandardCharsets.UTF_8));
    }

    @Test
    public void testDecodeUrlSafe() {

        assertEquals("aa>", new String(Base64Codec.decode("YWE-"), StandardCharsets.UTF_8));
        assertEquals("bb?", new String(Base64Codec.decode("YmI_"), StandardCharsets.UTF_8));

        assertEquals("", new String(Base64Codec.decode(""), StandardCharsets.UTF_8));
        assertEquals("f", new String(Base64Codec.decode("Zg"), StandardCharsets.UTF_8));
        assertEquals("fo", new String(Base64Codec.decode("Zm8"), StandardCharsets.UTF_8));
        assertEquals("foo", new String(Base64Codec.decode("Zm9v"), StandardCharsets.UTF_8));
        assertEquals("foob", new String(Base64Codec.decode("Zm9vYg"), StandardCharsets.UTF_8));
        assertEquals("fooba", new String(Base64Codec.decode("Zm9vYmE"), StandardCharsets.UTF_8));
        assertEquals("foobar", new String(Base64Codec.decode("Zm9vYmFy"), StandardCharsets.UTF_8));
    }

    @Test
    public void testDecodeUrlSafeWithIllegalChars() {

        assertEquals("", new String(Base64Codec.decode("\n"), StandardCharsets.UTF_8));
        assertEquals("f", new String(Base64Codec.decode("Zg\n"), StandardCharsets.UTF_8));
        assertEquals("fo", new String(Base64Codec.decode("Zm8\n"), StandardCharsets.UTF_8));
        assertEquals("foo", new String(Base64Codec.decode("Zm9v\n"), StandardCharsets.UTF_8));
        assertEquals("foob", new String(Base64Codec.decode("Zm9vYg\n"), StandardCharsets.UTF_8));
        assertEquals("fooba", new String(Base64Codec.decode("Zm9vYmE\n"), StandardCharsets.UTF_8));
        assertEquals("foobar", new String(Base64Codec.decode("Zm9vYmFy\n"), StandardCharsets.UTF_8));
    }
}
