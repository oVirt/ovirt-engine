package org.ovirt.engine.core.compat;

import org.ovirt.engine.core.compat.backendcompat.PropertyCompat;
import org.ovirt.engine.core.compat.backendcompat.TypeCompat;

import junit.framework.TestCase;

public class PropertyCompatTest extends TestCase {
    public String someValue = "Anakin";

    public void testIt() {
        PropertyCompat pc = TypeCompat.GetProperty(this.getClass(), "someValue");
        String result = (String) pc.GetValue(this, "JarJar");
        assertEquals("Standard", "Anakin", result);
        this.someValue = null;
        result = (String) pc.GetValue(this, "JarJar");
        assertEquals("Null Value", "JarJar", result);
    }

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

}
