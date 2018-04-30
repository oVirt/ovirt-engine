package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;

public class PropertyInfoTest {
    private String someValue;

    @Test
    public void testGetAvailableValues() {
        setSomeValue("Anakin");
        Map<String, String> values = new HashMap<>();
        Set<String> properties = Collections.singleton("somevalue");
        TypeCompat.getPropertyValues(this, properties, values);
        assertFalse(properties.isEmpty());
        assertEquals("Anakin", values.get("somevalue"), "Standard");
    }

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

}
