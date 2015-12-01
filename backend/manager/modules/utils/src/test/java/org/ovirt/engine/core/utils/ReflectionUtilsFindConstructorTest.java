package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for the {@link ReflectionUtils} class.
 *
 */
@RunWith(Parameterized.class)
public class ReflectionUtilsFindConstructorTest {

    /* --- Class Fields --- */

    /** The type to test the method with. */
    private final Class<?> typeToTest;

    /**
     * The parameters used to look for an empty ctor (unless non-static inner class, should be an empty array).
     */
    private final Class<?>[] parametersForEmptyCase;

    /** The parameters used to look for the exact case. */
    private final Class<?>[] parametersForExactCase;

    /** The parameters used for the negative case of super type. */
    private final Class<?>[] parametersForSupertypeCase;

    /** The parameters used for the positive case of sub type. */
    private final Class<?>[] parametersForSubtypeCase;

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

    /* --- Constructors --- */

    /**
     * Construct a test case for the given test data.
     *
     * @param typeToTest
     *            The type to test the method with.
     * @param parametersForEmptyCase
     *            The parameters used to look for an empty ctor (unless non-static inner class, should be an empty
     *            array).
     * @param parametersForExactCase
     *            The parameters used to look for the exact case.
     * @param parametersForSupertypeCase
     *            The parameters used for the negative case of super type.
     * @param parametersForSubtypeCase
     *            The parameters used for the positive case of sub type.
     */
    public ReflectionUtilsFindConstructorTest(Class<?> typeToTest,
                                              Class<?>[] parametersForEmptyCase,
                                              Class<?>[] parametersForExactCase,
                                              Class<?>[] parametersForSupertypeCase,
                                              Class<?>[] parametersForSubtypeCase) {
        this.typeToTest = typeToTest;
        this.parametersForEmptyCase = parametersForEmptyCase;
        this.parametersForExactCase = parametersForExactCase;
        this.parametersForSupertypeCase = parametersForSupertypeCase;
        this.parametersForSubtypeCase = parametersForSubtypeCase;
    }

    /* --- Parameters generation --- */

    @Parameters
    public static Collection<Object[]> parameters() {
        Collection<Object[]> ret = new ArrayList<>(5);

        ret.add(new Object[] { ClassWithEmptyPublicCtor.class, new Class<?>[0], new Class<?>[] { Exception.class },
                new Class<?>[] { Throwable.class }, new Class<?>[] { RuntimeException.class } });
        ret.add(new Object[] { ClassWithEmptyProtectedCtor.class, new Class<?>[0], new Class<?>[] { Exception.class },
                new Class<?>[] { Throwable.class }, new Class<?>[] { RuntimeException.class } });
        ret.add(new Object[] { ClassWithEmptyPrivateCtor.class, new Class<?>[0], new Class<?>[] { Exception.class },
                new Class<?>[] { Throwable.class }, new Class<?>[] { RuntimeException.class } });
        ret.add(new Object[] { GenericizedClass.class, new Class<?>[0], new Class<?>[] { Exception.class },
                new Class<?>[] { Throwable.class }, new Class<?>[] { RuntimeException.class } });
        ret.add(new Object[] { ClassWithEmptyPublicCtor.InnerClass.class,
                new Class<?>[] { ClassWithEmptyPublicCtor.class },
                new Class<?>[] { ClassWithEmptyPublicCtor.class, Exception.class },
                new Class<?>[] { ClassWithEmptyPublicCtor.class, Throwable.class },
                new Class<?>[] { ClassWithEmptyPublicCtor.class, RuntimeException.class } });
        ret.add(new Object[] { ClassWithEmptyPublicCtor.StaticInnerClass.class, new Class<?>[0],
                new Class<?>[] { Exception.class }, new Class<?>[] { Throwable.class },
                new Class<?>[] { RuntimeException.class } });

        return ret;
    }

    /* --- Tests for the positive cases --- */

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} finds the constructor when no parameter types
     * are passed.
     */
    @Test
    public void positiveTestFindConstructorForNoParams() throws Exception {
        findConstructorAndAssertItWasFound(parametersForEmptyCase);
    }

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} finds the constructor when the exact parameter
     * is passed.
     */
    @Test
    public void positiveTestFindConstructorForExactParam() throws Exception {
        findConstructorAndAssertItWasFound(parametersForExactCase);
    }

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} finds the constructor when the subtype of the
     * parameter is passed.
     */
    @Test
    public void positiveTestFindConstructorForSubtypeOfParam() throws Exception {
        findConstructorAndAssertItWasFound(parametersForSubtypeCase);
    }

    /* --- Tests for the negative cases --- */

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} doesn't find a constructor when the supertype
     * is used for lookup.
     */
    @Test
    public void negativeTestFindConstructorForSupertype() throws Exception {
        assertNull(createAssertMessage("ctor was found anyway"),
                   ReflectionUtils.findConstructor(typeToTest, parametersForSupertypeCase));
    }

    /**
     * Test that {@link ReflectionUtils#findConstructor(Class, Class...)} doesn't find a constructor when the number of
     * arguments doesn't match anything.
     */
    @Test
    public void negativeTestFindConstructorForMismatchingArgumentsNumber() throws Exception {
        assertNull(createAssertMessage("ctor was found anyway"),
                   ReflectionUtils.findConstructor(typeToTest,
                                                   (Class<?>[]) ArrayUtils.add(parametersForExactCase, Object.class)));
    }

    /* --- Helper Methods --- */

    /**
     * Find the constructor with the given parameters and check that it was found and has the correct number and type of
     * parameters.
     *
     * @param parameters
     *            The parameters to check with.
     */
    private void findConstructorAndAssertItWasFound(Class<?>[] parameters) {
        Constructor<?> constructor = ReflectionUtils.findConstructor(typeToTest, parameters);
        assertNotNull(createAssertMessage("ctor not found"), constructor);
        assertEquals(createAssertMessage("found ctor with wrong parameter length"), parameters.length,
                     constructor.getParameterTypes().length);

        Class<?>[] ctorParameters = constructor.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            assertTrue(createAssertMessage("parameter not compatible, expected {1} or a subclass, but was {2}",
                                          parameters[i].getSimpleName(),
                                          ctorParameters[i].getSimpleName()),
                       ctorParameters[i].isAssignableFrom(parameters[i]));
        }
    }

    /**
     * Create an assertion message with the class type that was checked, because the parameterized tests don't have this
     * info.
     *
     * @param message
     *            Additional message to print.
     * @param arguments
     *            Additional arguments (if any).
     *
     * @return The assertion message.
     */
    private String createAssertMessage(String message, Object... arguments) {
        return MessageFormat.format("Test for type {0} failed: " + message,
                                    ArrayUtils.addAll(new Object[] { typeToTest.getSimpleName() }, arguments));
    }
}
