package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.compat.backendcompat.PropertyInfo;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;

public class PropertyInfoTest {
    private String someValue = "Anakin";

    @Test
    public void testIt() {
        PropertyInfo propInfo = TypeCompat.GetProperty(this.getClass(), "someValue");
        String result = (String) propInfo.getValue(this, "JarJar");
        assertEquals("Standard", "Anakin", result);
        this.someValue = null;
        result = (String) propInfo.getValue(this, "JarJar");
        assertEquals("Null Value", "JarJar", result);
    }

    @Test
    public void testGetAvailableValues() {
        Map<String, String> values = new HashMap<String, String>();
        Set<String> properties = new HashSet<String>(Arrays.asList("somevalue"));
        TypeCompat.getPropertyValues(this, properties, values);
        assertFalse(properties.isEmpty());
        assertEquals("Standard", "Anakin", values.get("somevalue"));
    }

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

}
