package org.ovirt.engine.core.common.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IPv4MaskValidatorTest {
    @ParameterizedTest
    @MethodSource
    public void netmaskFormatValidation(String mask, boolean isNetmaskValidFormat) {
        assertEquals(isNetmaskValidFormat, IPv4MaskValidator.getInstance().isValidNetmaskFormat(mask),
                "Failed to validate mask's Format: " + mask);
    }

    public static Stream<Arguments> netmaskFormatValidation() {
        return namesParams().map(o -> Arguments.of(o[0], o[1]));
    }

    @ParameterizedTest
    @MethodSource
    public void prefixFormatValidation(String mask, boolean isPrefixValid) {
        assertEquals(isPrefixValid, IPv4MaskValidator.getInstance().isPrefixValid(mask),
                "Failed to validate prefix's Format: " + mask);
    }

    public static Stream<Arguments> prefixFormatValidation() {
        return namesParams().map(o -> Arguments.of(o[0], o[3]));
    }

    @ParameterizedTest
    @MethodSource
    public void netmaskValidValue(String mask, boolean isNetmaskValidValue) {
        assertEquals(isNetmaskValidValue, IPv4MaskValidator.getInstance().isNetmaskValid(mask),
                "Failed to validate mask value" + mask);
    }

    public static Stream<Arguments> netmaskValidValue() {
        return namesParams()
                .filter(o -> IPv4MaskValidator.getInstance().isValidNetmaskFormat((String) o[0]))
                .map(o -> Arguments.of(o[0], o[2]));
    }

    public static Stream<Object[]> namesParams() {
         Random random = new Random();

        return Stream.of(

                // Bad Format
                new Object[] { null, false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "a.a.a.a", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "255.255.0", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "255.255.0.0.0", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "255.255.0.0.", false, random.nextBoolean(), false }, //$NON-NLS-1$

                new Object[] { "31 ", false, random.nextBoolean(), false }, //$NON-NLS-1$ /*note extra space*/
                new Object[] { "/31 ", false, random.nextBoolean(), false }, //$NON-NLS-1$ /*note extra space*/
                new Object[] { "31/", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "31*", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "//31 ", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "33", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "/33", false, random.nextBoolean(), false }, //$NON-NLS-1$


                new Object[] { "01", false, random.nextBoolean(), false }, //$NON-NLS-1$
                new Object[] { "01/", false, random.nextBoolean(), false }, //$NON-NLS-1$


                // Not Valid
                new Object[] { "255.255.0.1", true, false, false }, //$NON-NLS-1$
                new Object[] { "255.0.255.0", true, false, false }, //$NON-NLS-1$
                new Object[] { "255.0.0.255", true, false, false }, //$NON-NLS-1$
                new Object[] { "224.0.255.0", true, false, false }, //$NON-NLS-1$

                // Valid
                new Object[] { "255.255.0.0", true, true, false }, //$NON-NLS-1$
                new Object[] { "255.255.255.255", true, true, false }, //$NON-NLS-1$

                new Object[] { "31", false, random.nextBoolean(), true }, //$NON-NLS-1$
                new Object[] { "/31", false, random.nextBoolean(), true }, //$NON-NLS-1$
                new Object[] { "7", false, random.nextBoolean(), true }, //$NON-NLS-1$
                new Object[] { "/7", false, random.nextBoolean(), true } //$NON-NLS-1$

        );
    }


    @ParameterizedTest
    @MethodSource
    public void testOctetNetmask(String mask, String expected) {
        assertEquals(IPv4MaskValidator.getInstance().getOctetNetmask(mask), expected);
    }

    public static Stream<Object[]> testOctetNetmask() {

        return Stream.of(

                new Object[] { "255.255.0.0", "255.255.0.0" }, //$NON-NLS-1$
                new Object[] { "255.255.255.0", "255.255.255.0" }, //$NON-NLS-1$
                new Object[] { "255.255.255.255", "255.255.255.255" }, //$NON-NLS-1$
                new Object[] { "255.255.124.0", "255.255.124.0" }, //$NON-NLS-1$

                new Object[] { "32", "255.255.255.255" }, //$NON-NLS-1$
                new Object[] { "31", "255.255.255.254" }, //$NON-NLS-1$
                new Object[] { "24", "255.255.255.0" }, //$NON-NLS-1$
                new Object[] { "21", "255.255.248.0" }, //$NON-NLS-1$
                new Object[] { "16", "255.255.0.0" }, //$NON-NLS-1$
                new Object[] { "13", "255.248.0.0" }, //$NON-NLS-1$
                new Object[] { "8", "255.0.0.0" }, //$NON-NLS-1$
                new Object[] { "5", "248.0.0.0" } //$NON-NLS-1$
        );
    }
}
