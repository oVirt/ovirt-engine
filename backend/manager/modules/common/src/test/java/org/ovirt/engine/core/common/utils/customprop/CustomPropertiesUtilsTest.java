package org.ovirt.engine.core.common.utils.customprop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.ovirt.engine.core.common.utils.customprop.PropertiesUtilsTestHelper.validatePropertyMap;
import static org.ovirt.engine.core.common.utils.customprop.PropertiesUtilsTestHelper.validatePropertyValue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test for common properties method
 */
public class CustomPropertiesUtilsTest {
    /**
     * Tests syntax error on properties with string format
     */
    @Test
    public void stringPropertiesSyntax() {
        CustomPropertiesUtils utils = new CustomPropertiesUtils();

        assertTrue(utils.syntaxErrorInProperties("speed:1024;duplex=full"), "Invalid key/value delimiter");
        assertTrue(utils.syntaxErrorInProperties("speed=1024;duplex"), "Missing key/value delimiter");
        assertTrue(utils.syntaxErrorInProperties("spe*ed=1024;duplex=full"), "Invalid key character");
        assertTrue(utils.syntaxErrorInProperties("speed=1024;duplex=fu;ll"), "Invalid value character");
    }

    /**
     * Tests converting device properties from a map to a string
     */
    @Test
    public void mapPropertiesSyntaxNullValue() {
        CustomPropertiesUtils utils = new CustomPropertiesUtils();
        Map<String, String> propMap = new LinkedHashMap<>();
        propMap.put("speed", "1024");
        propMap.put("duplex", null);
        propMap.put("debug", "");

        assertFalse(utils.syntaxErrorInProperties(propMap));
    }

    /**
     * Tests converting valid properties from {@code String} to {@code Map<String, String>}
     */
    @Test
    public void convertValidPropertiesToMap() {
        CustomPropertiesUtils utils = new CustomPropertiesUtils();
        String propStr = "speed=1024;duplex=half;debug=";

        Map<String, String> map = utils.convertProperties(propStr);
        validatePropertyMap(map, 3);
        validatePropertyValue(map, "speed", "1024");
        validatePropertyValue(map, "duplex", "half");
        validatePropertyValue(map, "debug", "");
    }

    /**
     * Tests converting valid properties from {@code Map<String, String>} to {@code String}
     */
    @Test
    public void convertValidPropertiesToString() {
        CustomPropertiesUtils utils = new CustomPropertiesUtils();
        Map<String, String> propMap = new LinkedHashMap<>();
        propMap.put("speed", "1024");
        propMap.put("duplex", "half");
        propMap.put("debug", null);
        propMap.put("verbose", "");

        String propStr = utils.convertProperties(propMap);
        // order of properties in string is known if LinkedHashMap is used
        assertEquals("speed=1024;duplex=half;debug=;verbose=", propStr);
    }
}
