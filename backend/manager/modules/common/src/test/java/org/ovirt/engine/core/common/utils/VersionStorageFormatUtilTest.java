package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertNotNull;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.compat.Version;

/**
 * A test case for {@link VersionStorageFormatUtil}. This test was put in place to ensure that no new entries would be
 * added to {@link Version#ALL} without {@link VersionStorageFormatUtil} being updated.
 */
@RunWith(Theories.class)
public class VersionStorageFormatUtilTest {
    @DataPoints
    public static final Version[] versions = Version.ALL.toArray(new Version[Version.ALL.size()]);

    @Theory
    public void versionHasMatchingFormat(Version v) {
        assertNotNull("Missing format for version " + v, VersionStorageFormatUtil.getFormatForVersion(v));
    }
}
