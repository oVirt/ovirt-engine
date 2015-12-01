package org.ovirt.engine.core.common.utils.customprop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.common.utils.customprop.PropertiesUtilsTestHelper.validatePropertyMap;
import static org.ovirt.engine.core.common.utils.customprop.PropertiesUtilsTestHelper.validatePropertyValue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

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

        assertTrue("Invalid key/value delimiter", utils.syntaxErrorInProperties("speed:1024;duplex=full"));
        assertTrue("Missing key/value delimiter", utils.syntaxErrorInProperties("speed=1024;duplex"));
        assertTrue("Invalid key character", utils.syntaxErrorInProperties("spe*ed=1024;duplex=full"));
        assertTrue("Invalid value character", utils.syntaxErrorInProperties("speed=1024;duplex=fu;ll"));
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
