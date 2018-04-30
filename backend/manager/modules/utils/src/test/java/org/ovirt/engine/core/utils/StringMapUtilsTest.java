package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class StringMapUtilsTest {

    private static final String ONE_KEY_VAL = "key1=val1";
    private static final String MUL_KEY_VAL = "key1=val1,key2=val2,key3=val3,key4=val4";
    private static final String EMPTY_KEY_VAL = "key1=val1,key2=";
    private static final String EMPTY_SIGN_KEY_VAL = "This string has no equal sign for the key";
    private static final String NULL_KEY_VAL = null;

    @Test
    public void testOneKeyVal() {
        Map<String, String> map = StringMapUtils.string2Map(ONE_KEY_VAL);
        assertEquals(1, map.size());
    }

    @Test
    public void testMulKeyVal() {
        Map<String, String> map = StringMapUtils.string2Map(MUL_KEY_VAL);
        assertEquals(4, map.size());
    }

    @Test
    public void testEmptyKeyVal() {
        Map<String, String> map = StringMapUtils.string2Map(EMPTY_KEY_VAL);
        assertEquals(2, map.size());
    }

    @Test
    public void testEmptySignKeyVal() {
        Map<String, String> map = StringMapUtils.string2Map(EMPTY_SIGN_KEY_VAL);
        assertEquals(1, map.size());
    }

    @Test
    public void testNullKeyVal() {
        Map<String, String> map = StringMapUtils.string2Map(NULL_KEY_VAL);
        assertEquals(0, map.size());
    }
}
