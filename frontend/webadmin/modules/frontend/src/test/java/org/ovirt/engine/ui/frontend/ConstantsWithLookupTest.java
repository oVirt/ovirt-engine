package org.ovirt.engine.ui.frontend;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineMessage;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public class ConstantsWithLookupTest {
    public static Stream<Arguments> nameInEnum
            (Class<? extends Enum> enumClass, Class<? extends ConstantsWithLookup> constantsClass) {
        return Arrays.stream(constantsClass.getMethods())
                .filter(m -> m.getParameterTypes().length == 0 && m.getReturnType().equals(String.class))
                .map(Method::getName)
                .map(n -> Arguments.of(n, enumClass));
    }

    public static Stream<Arguments> nameInEnum() {
        return Stream.concat(
                nameInEnum(EngineMessage.class, AppErrors.class),
                nameInEnum(EngineError.class, VdsmErrors.class)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void nameInEnum(String methodName, Class<? extends Enum> appErrorsClass) {
        // Throws an exception if the method name does not represent a constant in the enum
        Enum.valueOf(appErrorsClass, methodName);
    }
}
