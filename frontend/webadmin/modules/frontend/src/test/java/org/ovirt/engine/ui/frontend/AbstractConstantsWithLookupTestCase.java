package org.ovirt.engine.ui.frontend;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.experimental.theories.Theory;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public class AbstractConstantsWithLookupTestCase {
    protected static List<String> methodNames(Class<? extends ConstantsWithLookup> constantsClass) {
        return Arrays.stream(constantsClass.getMethods())
                .filter(m -> m.getParameterTypes().length == 0 && m.getReturnType().equals(String.class))
                .map(Method::getName)
                .collect(Collectors.toList());
    }

    @Theory
    public void nameInEnum(String methodName, Class<? extends Enum> appErrorsClass) {
        // Throws an exception if the method name does not represent a constant in the enum
        Enum.valueOf(appErrorsClass, methodName);
    }
}
