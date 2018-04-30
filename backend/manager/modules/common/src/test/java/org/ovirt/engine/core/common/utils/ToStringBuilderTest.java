package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ToStringBuilderTest {
    /**
     * If no class and attributes were specified, then empty string should be returned
     */
    @Test
    public void testEmptyForClass() {
        assertEquals(
                "",
                ToStringBuilder.forClass(null).build());
    }

    /**
     * If class without attributes was specified, then class name with empty attributes list should be returned
     */
    @Test
    public void testClassOnlyOutput() {
        String expected = getClass().getSimpleName()
                + ToStringBuilder.CLASS_NAME_SUFFIX
                + ToStringBuilder.ATTRIBUTES_LIST_PREFIX
                + ToStringBuilder.ATTRIBUTES_LIST_SUFFIX;

        assertEquals(
                expected,
                ToStringBuilder.forClass(ToStringBuilderTest.class).build());
    }

    /**
     * If no instance and attributes were specified, then empty string should be returned
     */
    @Test
    public void testEmptyForInstance() {
        assertEquals(
                "",
                ToStringBuilder.forInstance(null).build());
    }

    /**
     * If instance without attributes was specified, then class name with empty attributes list should be returned
     */
    @Test
    public void testInstanceOnlyOutput() {
        String expected = getClass().getSimpleName()
                + ToStringBuilder.CLASS_NAME_SUFFIX
                + ToStringBuilder.ATTRIBUTES_LIST_PREFIX
                + ToStringBuilder.ATTRIBUTES_LIST_SUFFIX;

        assertEquals(
                expected,
                ToStringBuilder.forInstance(this).build());
    }

    /**
     * If attributes without class/instance were specified, then attributes list should be returned
     */
    @Test
    public void testAttributeOnlyOutput() {
        String attributeName = "attr1";
        int attributeValue = 100;
        String expected = ToStringBuilder.ATTRIBUTES_LIST_PREFIX
                + createFirstAttributeOutput(attributeName, String.valueOf(attributeValue))
                + ToStringBuilder.ATTRIBUTES_LIST_SUFFIX;

        assertEquals(
                expected,
                ToStringBuilder.forInstance(null)
                        .append(attributeName, attributeValue)
                        .build());
    }


    /**
     * Tests valid output for all attribute types
     */
    @Test
    public void testAttributesConversion() {
        final String booleanName = "booleanAttr";
        final boolean booleanValue = true;
        final String booleanArrayName = "booleanArrayAttr";
        final boolean[] booleanArrayValue = {true, false};
        final String byteName = "byteAttr";
        final byte byteValue = 1;
        final String byteArrayName = "byteArrayAttr";
        final byte[] byteArrayValue = {2, 3};
        final String charName = "charAttr";
        final char charValue = 'a';
        final String charArrayName = "charArrayAttr";
        final char[] charArrayValue = {'b', 'c'};
        final String doubleName = "doubleAttr";
        final double doubleValue = 0.1d;
        final String doubleArrayName = "doubleArrayAttr";
        final double[] doubleArrayValue = {0.2d, 0.3d};
        final String floatName = "floatAttr";
        final float floatValue = 0.5f;
        final String floatArrayName = "floatArrayAttr";
        final float[] floatArrayValue = {0.6f, 0.7f};
        final String intName = "intAttr";
        final int intValue = 100;
        final String intArrayName = "intArrayAttr";
        final int[] intArrayValue = {110, 120};
        final String longName = "longAttr";
        final long longValue = 1000L;
        final String longArrayName = "longArrayAttr";
        final long[] longArrayValue = {1001L, 1002L};
        final String objectName = "objectAttr";
        final Object objectValue = new BigDecimal("123.45");
        final String objectArrayName = "objectArrayAttr";
        final Object[] objectArrayValue = {new BigDecimal("56.7"), new Date()};
        final String shortName = "shortAttr";
        final short shortValue = 10;
        final String shortArrayName = "shortArrayAttr";
        final short[] shortArrayValue = {11, 12};
        final String passwordName = "password";
        final String passwordValue = "qwerty";
        final String nullPasswordName = "nullPassword";
        final String nullPasswordValue = null;
        final String nullName = null;
        final String nullValue = null;
        final String nullArrayName = "nullArray";
        final Object[] nullArrayValue = null;
        final String nullCollectionName = "nullCollection";
        final Collection<Object> nullCollectionValue = null;
        final String emptyCollectionName = "emptyCollection";
        final Collection<Object> emptyCollectionValue = Collections.emptyList();
        final String collectionName = "collection";
        final Collection<String> collectionValue = Arrays.asList("item1", "item2");
        final String nullMapName = "nullMap";
        final Map<Object, Object> nullMapValue = null;
        final String emptyMapName = "emptyMap";
        final Map<Object, Object> emptyMapValue = Collections.emptyMap();
        final String mapName = "map";
        final Map<String, String> mapValue = new HashMap<>();
        mapValue.put("key1", "value1");
        mapValue.put("key2", "value2");

        String expected = ToStringBuilder.ATTRIBUTES_LIST_PREFIX
                + createFirstAttributeOutput(booleanName, String.valueOf(booleanValue))
                + createAttributeOutput(booleanArrayName, Arrays.toString(booleanArrayValue))
                + createAttributeOutput(byteName, String.valueOf(byteValue))
                + createAttributeOutput(byteArrayName, Arrays.toString(byteArrayValue))
                + createAttributeOutput(charName, String.valueOf(charValue))
                + createAttributeOutput(charArrayName, Arrays.toString(charArrayValue))
                + createAttributeOutput(doubleName, String.valueOf(doubleValue))
                + createAttributeOutput(doubleArrayName, Arrays.toString(doubleArrayValue))
                + createAttributeOutput(floatName, String.valueOf(floatValue))
                + createAttributeOutput(floatArrayName, Arrays.toString(floatArrayValue))
                + createAttributeOutput(intName, String.valueOf(intValue))
                + createAttributeOutput(intArrayName, Arrays.toString(intArrayValue))
                + createAttributeOutput(longName, String.valueOf(longValue))
                + createAttributeOutput(longArrayName, Arrays.toString(longArrayValue))
                + createAttributeOutput(objectName, String.valueOf(objectValue))
                + createAttributeOutput(objectArrayName, Arrays.toString(objectArrayValue))
                + createAttributeOutput(shortName, String.valueOf(shortValue))
                + createAttributeOutput(shortArrayName, Arrays.toString(shortArrayValue))
                + createAttributeOutput(nullName, String.valueOf(nullValue))
                + createAttributeOutput(nullArrayName, Arrays.toString(nullArrayValue))
                + createAttributeOutput(passwordName, ToStringBuilder.FILTERED_CONTENT)
                + createAttributeOutput(nullPasswordName, nullPasswordValue)
                + createAttributeOutput(nullCollectionName, null)
                + createAttributeOutput(emptyCollectionName, Arrays.toString(emptyCollectionValue.toArray()))
                + createAttributeOutput(collectionName, Arrays.toString(collectionValue.toArray()))
                + createAttributeOutput(nullMapName, null)
                + createAttributeOutput(emptyMapName, Arrays.toString(emptyMapValue.entrySet().toArray()))
                + createAttributeOutput(mapName, Arrays.toString(mapValue.entrySet().toArray()))
                + ToStringBuilder.ATTRIBUTES_LIST_SUFFIX;

        assertEquals(
                expected,
                ToStringBuilder.forInstance(null)
                        .append(booleanName, booleanValue)
                        .append(booleanArrayName, booleanArrayValue)
                        .append(byteName, byteValue)
                        .append(byteArrayName, byteArrayValue)
                        .append(charName, charValue)
                        .append(charArrayName, charArrayValue)
                        .append(doubleName, doubleValue)
                        .append(doubleArrayName, doubleArrayValue)
                        .append(floatName, floatValue)
                        .append(floatArrayName, floatArrayValue)
                        .append(intName, intValue)
                        .append(intArrayName, intArrayValue)
                        .append(longName, longValue)
                        .append(longArrayName, longArrayValue)
                        .append(objectName, objectValue)
                        .append(objectArrayName, objectArrayValue)
                        .append(shortName, shortValue)
                        .append(shortArrayName, shortArrayValue)
                        .append(nullName, nullValue)
                        .append(nullArrayName, nullArrayValue)
                        .appendFiltered(passwordName, passwordValue)
                        .appendFiltered(nullPasswordName, nullPasswordValue)
                        .append(nullCollectionName, nullCollectionValue)
                        .append(emptyCollectionName, emptyCollectionValue)
                        .append(collectionName, collectionValue)
                        .append(nullMapName, nullMapValue)
                        .append(emptyMapName, emptyMapValue)
                        .append(mapName, mapValue)
                        .build());
    }

    private String createAttributeOutput(String name, String value) {
        return ToStringBuilder.ATTRIBUTES_SEPARATOR
                + createFirstAttributeOutput(name, value);
    }

    private String createFirstAttributeOutput(String name, String value) {
        return name
                + ToStringBuilder.NAME_VALUE_SEPARATOR
                + value
                + ToStringBuilder.VALUE_SUFFIX;
    }
}
