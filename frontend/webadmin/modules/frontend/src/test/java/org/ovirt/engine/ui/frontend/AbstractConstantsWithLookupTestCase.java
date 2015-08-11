package org.ovirt.engine.ui.frontend;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.experimental.theories.Theory;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public class AbstractConstantsWithLookupTestCase {
    protected static String[] methodNames(Class<? extends ConstantsWithLookup> constantsClass) {
        List<String> names = new LinkedList<>();

        for (Method m : constantsClass.getMethods()) {
            if (m.getParameterTypes().length == 0 && m.getReturnType().equals(String.class)) {
                names.add(m.getName());
            }
        }

        return names.toArray(new String[names.size()]);
    }

    @Theory
    public void nameInEnum(String methodName, Class<? extends Enum> appErrorsClass) {
        // Throws an exception if the method name does not represent a constant in the enum
        Enum.valueOf(appErrorsClass, methodName);
    }
}
