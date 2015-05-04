package org.ovirt.engine.ui.common.utils;

/**
 * Helper class for working with floating point numbers, such as JavaScript {@code number}
 * and its Java {@code double} equivalent.
 */
public class FloatingPointHelper {

    /**
     * Machine epsilon for JavaScript {@code number} type, lazily initialized to value
     * {@code pow(2, -53)}. This number represents an upper bound on the relative error
     * due to rounding in floating point arithmetic.
     */
    private static double epsilon;

    /**
     * Returns {@code true} if {@code a} and {@code b} are numerically equal using
     * {@linkplain #epsilon machine epsilon} to account for rounding procedure.
     *
     * @return {@code true} if {@code a} is numerically equal to {@code b}.
     */
    public static native boolean epsEqual(Double a, Double b) /*-{
        var aValue = a.@java.lang.Double::doubleValue()();
        var bValue = b.@java.lang.Double::doubleValue()();

        if (!@org.ovirt.engine.ui.common.utils.FloatingPointHelper::epsilon) {
            @org.ovirt.engine.ui.common.utils.FloatingPointHelper::epsilon = Math.pow(2, -53);
        }

        return Math.abs(aValue - bValue) < @org.ovirt.engine.ui.common.utils.FloatingPointHelper::epsilon;
    }-*/;

    /**
     * Compares {@code a} and {@code b} using {@linkplain #epsilon machine epsilon}
     * to account for rounding procedure.
     *
     * @return {@code 0} if {@code a} is numerically equal to {@code b},
     *         value less than {@code 0} if {@code a} is numerically less than {@code b},
     *         value greater than {@code 0} if {@code a} is numerically greater than {@code b}.
     */
    public static int epsCompare(Double a, Double b) {
        if (epsEqual(a, b)) {
            return 0;
        }

        return (a < b) ? -1 : 1;
    }

    /**
     * {@link #epsCompare(Double, Double) epsCompare} method adapted for Float arguments.
     * <p>
     * In production mode, GWT implements all Java numeric types as JavaScript {@code number}
     * so any operations on {@code float} are performed as on {@code double} and will result
     * in more-than-expected precision.
     *
     * @return {@code 0} if {@code a} is numerically equal to {@code b},
     *         value less than {@code 0} if {@code a} is numerically less than {@code b},
     *         value greater than {@code 0} if {@code a} is numerically greater than {@code b}.
     */
    public static int epsCompare(Float a, Float b) {
        return epsCompare(a.doubleValue(), b.doubleValue());
    }

}
