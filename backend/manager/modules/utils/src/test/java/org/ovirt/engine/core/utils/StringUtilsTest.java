package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * The following tests covers the string2Map and map2String methods.
 */
public class StringUtilsTest {

    private final String ONE_KEY_VAL = "key1=val1";
    private final String MUL_KEY_VAL = "key1=val1,key2=val2";
    private final String EMPTY_KEY_VAL = "key1=val1,key2=";
    private final String EMPTY_SIGN_KEY_VAL = "This string has no equal sign for the key";
    private final String NULL_KEY_VAL = null;

    @Test
    public void testOneKeyVal() {
        Map<String, String> map = StringUtils.string2Map(ONE_KEY_VAL);
        assertEquals(map.size(), 1);
    }

    @Test
    public void testMulKeyVal() {
        Map<String, String> map = StringUtils.string2Map(MUL_KEY_VAL);
        assertEquals(map.size(), 2);
    }

    @Test
    public void testEmptyKeyVal() {
        Map<String, String> map = StringUtils.string2Map(EMPTY_KEY_VAL);
        assertEquals(map.size(), 2);
    }

    @Test
    public void testEmptySignKeyVal() {
        Map<String, String> map = StringUtils.string2Map(EMPTY_SIGN_KEY_VAL);
        assertEquals(map.size(), 1);
    }

    @Test
    public void testNullKeyVal() {
        Map<String, String> map = StringUtils.string2Map(NULL_KEY_VAL);
        assertEquals(map.size(), 0);
    }

    @Test
    public void testMap2StringSingleKey() {
        assertEquals(StringUtils.map2String(StringUtils.string2Map(ONE_KEY_VAL)), ONE_KEY_VAL);
    }

    @Test
    public void testMap2StringMultiKeys() {
        Map<String, String> string2Map =
                StringUtils.string2Map(StringUtils.map2String(StringUtils.string2Map(MUL_KEY_VAL)));
        Map<String, String> expectedMap = new HashMap<String, String>();
        expectedMap.put("key1","val1");
        expectedMap.put("key2","val2");
        assertEquals(expectedMap, string2Map);
    }

    @Test
    public void testMap2StringEmptyValue() {
        Map<String, String> string2Map =
                StringUtils.string2Map(StringUtils.map2String(StringUtils.string2Map(EMPTY_KEY_VAL)));
        Map<String, String> expectedMap = new HashMap<String, String>();
        expectedMap.put("key1", "val1");
        expectedMap.put("key2", "");
        assertEquals(expectedMap, string2Map);
    }

    @Test
    public void testMap2StringNoValues() {
        Map<String, String> string2Map =
                StringUtils.string2Map(StringUtils.map2String(StringUtils.string2Map(EMPTY_SIGN_KEY_VAL)));
        Map<String, String> expectedMap = new HashMap<String, String>();
        expectedMap.put(EMPTY_SIGN_KEY_VAL, "");
        assertEquals(expectedMap, string2Map);
    }
}
