package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.compat.backendcompat.PropertyCompat;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;

public class PropertyCompatTest {
    private String someValue = "Anakin";

    @Test
    public void testIt() {
        PropertyCompat pc = TypeCompat.GetProperty(this.getClass(), "someValue");
        String result = (String) pc.GetValue(this, "JarJar");
        assertEquals("Standard", "Anakin", result);
        this.someValue = null;
        result = (String) pc.GetValue(this, "JarJar");
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
