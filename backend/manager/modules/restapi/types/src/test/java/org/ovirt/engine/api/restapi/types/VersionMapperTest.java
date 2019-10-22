/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Version;

public class VersionMapperTest {
    /**
     * Checks that a null version string results in a null version object.
     */
    @Test
    public void testNullStringProducesNullVersion() {
        Version version = VersionMapper.fromVersionString(null);
        assertNull(version);
    }

    /**
     * Checks that an empty version string results in a version object, containing an empty string in the
     * {@code full_version} attribute.
     */
    @Test
    public void testEmptyStringProducesEmptyVersion() {
        Version version = VersionMapper.fromVersionString("");
        assertNotNull(version);
        assertEquals("", version.getFullVersion());
    }

    /**
     * Checks that a version string that doesn't match the version number pattern doesn't generate an exception, and
     * that the complete version string is reported in the {@code full_version} property.
     */
    @Test
    public void testNoMatchDoesntCrash() {
        Version version = VersionMapper.fromVersionString("ugly");
        assertNotNull(version);
        assertEquals("ugly", version.getFullVersion());
    }

    /**
     * Checks that the major version number is extracted correctly, regardless of what trailing text is contained in
     * the version string.
     */
    @Test
    public void testMajorWithTrailingText() {
        assertMajor(3, "3");
        assertMajor(3, "3.6");
        assertMajor(3, "3.6.2");
        assertMajor(3, "3.6.2.1");
        assertMajor(3, "3.6.2.1-23");
        assertMajor(3, "3.6.2.1-23.1");
        assertMajor(3, "3.6.2.1-23.1.alpha0");
    }

    /**
     * Checks that the major version number is extracted correctly even if it contains leading zeroes.
     */
    @Test
    public void testMajorWithLeadingZeroes() {
        assertMajor(3, "03");
        assertMajor(3, "003");
    }

    /**
     * Checks that the minor version number is extracted correctly, regardless of what trailing text is contained in
     * the version string.
     */
    @Test
    public void testMinorWithTrailingText() {
        assertMinor(null, "3");
        assertMinor(6, "3.6");
        assertMinor(6, "3.6.2");
        assertMinor(6, "3.6.2.1");
        assertMinor(6, "3.6.2.1-23");
        assertMinor(6, "3.6.2.1-23.1");
        assertMinor(6, "3.6.2.1-23.1.alpha0");
    }

    /**
     * Checks that the minor version number is extracted correctly even if it contains leading zeroes.
     */
    @Test
    public void testMinorWithLeadingZeroes() {
        assertMinor(6, "3.06");
        assertMinor(6, "3.006");
    }

    /**
     * Checks that the build version number is extracted correctly, regardless of what trailing text is contained in
     * the version string.
     */
    @Test
    public void testBuildWithTrailingText() {
        assertBuild(null, "3");
        assertBuild(null, "3.6");
        assertBuild(2, "3.6.2");
        assertBuild(2, "3.6.2.1");
        assertBuild(2, "3.6.2.1-23");
        assertBuild(2, "3.6.2.1-23.1");
        assertBuild(2, "3.6.2.1-23.1.alpha0");
    }

    /**
     * Checks that the build version number is extracted correctly even if it contains leading zeroes.
     */
    @Test
    public void testBuildWithLeadingZeroes() {
        assertBuild(2, "3.6.02");
        assertBuild(2, "3.6.002");
    }

    /**
     * Checks that the revision version number is extracted correctly, regardless of what trailing text is contained in
     * the version string.
     */
    @Test
    public void testRevisionWithTrailingText() {
        assertRevision(null, "3");
        assertRevision(null, "3.6");
        assertRevision(null, "3.6.2");
        assertRevision(null, "3.6.2.1");
        assertRevision(23, "3.6.2.1-23");
        assertRevision(23, "3.6.2.1-23.1");
        assertRevision(23, "3.6.2.1-23.1.alpha0");
    }

    /**
     * Checks that the build version number is extracted correctly even if it contains leading zeroes.
     */
    @Test
    public void testRevisionWithLeadingZeroes() {
        assertRevision(23, "3.6.2-023");
        assertRevision(23, "3.6.2-0023");
    }

    /**
     * Asserts that the major version number extracted from the given version text is the expected.
     *
     * @param expected the expected major version number
     * @param text the complete version string
     */
    private void assertMajor(Integer expected, String text) {
        Version version = VersionMapper.fromVersionString(text);
        assertNotNull(version);
        Integer actual = version.getMajor();
        assertEquals(expected, actual, "Incorrect major version number");
    }

    /**
     * Asserts that the minor version number extracted from the given version text is the expected.
     *
     * @param expected the expected minor version number
     * @param text the complete version string
     */
    private void assertMinor(Integer expected, String text) {
        Version version = VersionMapper.fromVersionString(text);
        assertNotNull(version);
        Integer actual = version.getMinor();
        assertEquals(expected, actual, "Incorrect minor version number");
    }

    /**
     * Asserts that the build version number extracted from the given version text is the expected.
     *
     * @param expected the expected build version number
     * @param text the complete version string
     */
    private void assertBuild(Integer expected, String text) {
        Version version = VersionMapper.fromVersionString(text);
        assertNotNull(version);
        Integer actual = version.getBuild();
        assertEquals(expected, actual, "Incorrect build version number");
    }

    /**
     * Asserts that the revision version number extracted from the given version text is the expected.
     *
     * @param expected the expected revision version number
     * @param text the complete version string
     */
    private void assertRevision(Integer expected, String text) {
        Version version = VersionMapper.fromVersionString(text);
        assertNotNull(version);
        Integer actual = version.getRevision();
        assertEquals(expected, actual, "Incorrect revision version number");
    }
}
