package org.ovirt.engine.core.compat.backendcompat;

import junit.framework.TestCase;

class Jedi {
    public String someValue = "Anakin";

    public String getSomeValue() {
        return someValue;
    }

    public void setSomeValue(String someValue) {
        this.someValue = someValue;
    }

}

public class PropertyInfoTest extends TestCase {

    public void testIt() {
        Jedi jedi = new Jedi();
        PropertyInfo[] props = TypeCompat.GetProperties(Jedi.class);
        assertEquals("size", 1, props.length); // One for someValue,
        PropertyInfo pi = props[0];
        assertEquals("name", "someValue", props[0].getName());
        String result = (String) pi.GetValue(jedi, "JarJar");
        assertEquals("Standard", "Anakin", result);
        result = (String) pi.GetValue(jedi, null);
        assertEquals("Standard passing in Null", "Anakin", result);
        jedi.someValue = null;
        result = (String) pi.GetValue(jedi, "JarJar");
        assertEquals("Null Value", "JarJar", result);

    }

}
