package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for the {@link ReflectionUtils} class.
 *
 */
public class ReflectionUtilsFindConstructorTest {
    /* --- Private classes used for testing the findConstructor method. --- */

    @SuppressWarnings("unused")
    private static class ClassWithEmptyPublicCtor {
        public ClassWithEmptyPublicCtor() {
        }

        public ClassWithEmptyPublicCtor(Exception unused) {
        }

        public class InnerClass {
            public InnerClass() {
            }

            public InnerClass(Exception unused) {
            }
        }

        public static class StaticInnerClass {
            public StaticInnerClass() {
            }

            public StaticInnerClass(Exception unused) {
            }
        }
    }

    @SuppressWarnings("unused")
    private static class ClassWithEmptyProtectedCtor {
        protected ClassWithEmptyProtectedCtor() {
        }

        protected ClassWithEmptyProtectedCtor(Exception unused) {
        }
    }

    private static class ClassWithEmptyPrivateCtor {
        private ClassWithEmptyPrivateCtor() {
        }

        private ClassWithEmptyPrivateCtor(Exception unused) {
        }
    }

    @SuppressWarnings("unused")
    private static class GenericizedClass<T extends Exception> {
        public GenericizedClass() {
        }

        public GenericizedClass(T param) {
        }
    }

    /* --- Tests for the positive cases --- */

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} finds the constructor when no parameter types
     * are passed.
     */
    @ParameterizedTest
    @MethodSource
    public void positiveTestFindConstructorForNoParams(Class<?> typeToTest, Class<?>[] ctorParams) {
        findConstructorAndAssertItWasFound(typeToTest, ctorParams);
    }

    public static Stream<Arguments> positiveTestFindConstructorForNoParams() {
        return Stream.of(
                Arguments.of(ClassWithEmptyPublicCtor.class, new Class<?>[0]),
                Arguments.of(ClassWithEmptyProtectedCtor.class, new Class<?>[0]),
                Arguments.of(ClassWithEmptyPrivateCtor.class, new Class<?>[0]),
                Arguments.of(GenericizedClass.class, new Class<?>[0]),
                Arguments.of(ClassWithEmptyPublicCtor.InnerClass.class, new Class<?>[]{ClassWithEmptyPublicCtor.class}),
                Arguments.of(ClassWithEmptyPublicCtor.StaticInnerClass.class, new Class<?>[0])
        );
    }

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} finds the constructor when the exact parameter
     * is passed.
     */
    @ParameterizedTest
    @MethodSource
    public void positiveTestFindConstructorForExactParam(Class<?> typeToTest, Class<?>[] ctorParams) {
        findConstructorAndAssertItWasFound(typeToTest, ctorParams);
    }

    public static Stream<Arguments> positiveTestFindConstructorForExactParam() {
        return Stream.of(
                Arguments.of(ClassWithEmptyPublicCtor.class, new Class<?>[] { Exception.class }),
                Arguments.of(ClassWithEmptyProtectedCtor.class, new Class<?>[] { Exception.class }),
                Arguments.of(ClassWithEmptyPrivateCtor.class, new Class<?>[] { Exception.class }),
                Arguments.of(GenericizedClass.class, new Class<?>[] { Exception.class }),
                Arguments.of(ClassWithEmptyPublicCtor.InnerClass.class, new Class<?>[] { ClassWithEmptyPublicCtor.class }),
                Arguments.of(ClassWithEmptyPublicCtor.StaticInnerClass.class, new Class<?>[] { Exception.class })
        );
    }

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} finds the constructor when the subtype of the
     * parameter is passed.
     */
    @ParameterizedTest
    @MethodSource
    public void positiveTestFindConstructorForSubtypeOfParam(Class<?> typeToTest, Class<?>[] ctorParams) {
        findConstructorAndAssertItWasFound(typeToTest, ctorParams);
    }

    public static Stream<Arguments> positiveTestFindConstructorForSubtypeOfParam() {
        return Stream.of(
                Arguments.of(ClassWithEmptyPublicCtor.class, new Class<?>[] { RuntimeException.class }),
                Arguments.of(ClassWithEmptyProtectedCtor.class, new Class<?>[] { RuntimeException.class }),
                Arguments.of(ClassWithEmptyPrivateCtor.class, new Class<?>[] { RuntimeException.class }),
                Arguments.of(GenericizedClass.class, new Class<?>[] { RuntimeException.class }),
                Arguments.of(ClassWithEmptyPublicCtor.InnerClass.class,
                        new Class<?>[] { ClassWithEmptyPublicCtor.class, RuntimeException.class }),
                Arguments.of(ClassWithEmptyPublicCtor.StaticInnerClass.class, new Class<?>[] { RuntimeException.class })
        );
    }


    /* --- Tests for the negative cases --- */

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} doesn't find a constructor when the supertype
     * is used for lookup.
     */
    @ParameterizedTest
    @MethodSource
    public void negativeTestFindConstructorForSupertype(Class<?> typeToTest, Class<?>[] ctorParams) {
        assertNull(ReflectionUtils.findConstructor(typeToTest, ctorParams),
                createAssertMessage(typeToTest, "ctor was found anyway"));
    }

    public static Stream<Arguments> negativeTestFindConstructorForSupertype() {
        return Stream.of(
                Arguments.of(ClassWithEmptyPublicCtor.class, new Class<?>[] { Throwable.class }),
                Arguments.of(ClassWithEmptyProtectedCtor.class, new Class<?>[] { Throwable.class }),
                Arguments.of(ClassWithEmptyPrivateCtor.class, new Class<?>[] { Throwable.class }),
                Arguments.of(GenericizedClass.class, new Class<?>[] { Throwable.class }),
                Arguments.of(ClassWithEmptyPublicCtor.InnerClass.class,
                        new Class<?>[] { ClassWithEmptyPublicCtor.class, Throwable.class }),
                Arguments.of(ClassWithEmptyPublicCtor.StaticInnerClass.class, new Class<?>[] { Throwable.class })
        );
    }

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} doesn't find a constructor when the number of
     * arguments doesn't match anything.
     */
    @ParameterizedTest
    @MethodSource("positiveTestFindConstructorForExactParam")
    public void negativeTestFindConstructorForMismatchingArgumentsNumber(Class<?> typeToTest, Class<?>[] ctorParams) {
        assertNull(ReflectionUtils.findConstructor
                (typeToTest, (Class<?>[]) ArrayUtils.add(ctorParams, Object.class)),
                createAssertMessage(typeToTest, "ctor was found anyway"));
    }


    /* --- Helper Methods --- */

    /**
     * Find the constructor with the given parameters and check that it was found and has the correct number and type of
     * parameters.
     *
     * @param parameters
     *            The parameters to check with.
     */
    private void findConstructorAndAssertItWasFound(Class<?> typeToTest, Class<?>[] parameters) {
        Constructor<?> constructor = ReflectionUtils.findConstructor(typeToTest, parameters);
        assertNotNull(constructor, createAssertMessage(typeToTest, "ctor not found"));
        assertEquals(parameters.length, constructor.getParameterTypes().length,
                createAssertMessage(typeToTest, "found ctor with wrong parameter length"));

        Class<?>[] ctorParameters = constructor.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            assertTrue(ctorParameters[i].isAssignableFrom(parameters[i]),
                    createAssertMessage(typeToTest, "parameter not compatible, expected {1} or a subclass, but was {2}",
                            parameters[i].getSimpleName(),
                            ctorParameters[i].getSimpleName()));
        }
    }

    /**
     * Create an assertion message with the class type that was checked, because the parameterized tests don't have this
     * info.
     *
     * @param typeToTest
     *             The type being tested.
     * @param message
     *            Additional message to print.
     * @param arguments
     *            Additional arguments (if any).
     *
     * @return The assertion message.
     */
    private String createAssertMessage(Class<?> typeToTest, String message, Object... arguments) {
        return MessageFormat.format("Test for type {0} failed: " + message,
                                    ArrayUtils.addAll(new Object[] { typeToTest.getSimpleName() }, arguments));
    }
}
