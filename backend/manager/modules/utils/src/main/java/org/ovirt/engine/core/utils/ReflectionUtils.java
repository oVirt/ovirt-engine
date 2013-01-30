package org.ovirt.engine.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;



/**
 * General utilities for using Java's reflection mechanism.
 *
 */
public class ReflectionUtils {

    /**
     * Find the first constructor of the given type which matches the expected parameters, which is the one which has
     * parameters that can be assigned from the expected parameters.<br>
     * <b>Note:</b> In case the type is a non-static inner class, then all it's constructors have the outer class as the
     * first parameter.<br>
     * <b>Warning:</b> The returned constructor may not be normally visible to the calling class, so use it with care.<br>
     * <br>
     * For example, suppose we have a class A and a class B which extends it:
     * <ul>
     * <li>If the class has 2 constructors accepting A and B (respectfully), and we look for one accepting A, then the A
     * constructor is returned.</li>
     * <li>If the class has 2 constructors accepting A and B (respectfully), and we look for one accepting B, then the
     * first matching constructor is returned (not guaranteed to be the same each time).</li>
     * <li>If the class has only a constructor accepting B, and we look for one accepting B, then the B constructor is
     * returned.</li>
     * <li>If the class has only a constructor accepting B, and we look for one accepting A, then <code>null</code> is
     * returned.</li>
     * <li>If the class has a default constructor and we don't expect any arguments, then it is returned.</li>
     * </ul>
     *
     * @param type
     *            The type to look up the constructor for.
     * @param expectedParams
     *            The expected parameters for the constructor.
     *
     * @return The right constructor, or <code>null</code> in none found.
     */
    public static <T> Constructor<T> findConstructor(Class<T> type,
                                          Class<?>... expectedParams) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) type.getDeclaredConstructors();

        for (Constructor<T> constructor : constructors) {
            if (isCompatible(expectedParams, constructor.getParameterTypes())) {
                return constructor;
            }
        }

        return null;
    }

    /**
     * Check if the actual parameters are compatible with (assignable from) the expected parameters.
     *
     * @param expected
     *            The expected parameters.
     * @param actual
     *            The actual parameters.
     *
     * @return <code>true</code> iff the actual parameters are the same length and are assignable from the expected
     *         ones.
     */
    private static boolean isCompatible(Class<?>[] expected, Class<?>[] actual) {
        if (expected.length != actual.length) {
            return false;
        }

        for (int i = 0; i < expected.length; i++) {
            if (!actual[i].isAssignableFrom(expected[i])) {
                return false;
            }
        }

        return true;
    }


    /**
     * @param className
     *            The class to load.
     * @return The class corresponding to the given classname.
     */
    public static Class<?> getClassFor(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * Get a Field Size annotation
     * @param f
     * @return
     */
    public static Annotation getSizeAnnotation(Field f) {
        Annotation[] annotations = (Annotation[]) f.getAnnotations();
        for(Annotation annotation : annotations){
            if(annotation instanceof javax.validation.constraints.Size){
                return annotation;
            }
        }
        return null;
    }
}
