package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
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
        StorageFormatType sft = VersionStorageFormatUtil.getForVersion(v);
        assertNotNull(String.format("Missing format for version %s", v), sft);

        Version earliestSupported = VersionStorageFormatUtil.getEarliestVersionSupported(sft);
        assertNotNull(String.format("Missing earliest version for format %s", sft), earliestSupported);

        assertTrue(
                String.format(
                        "Earliest supported version (%s) should no be later than the version requiring this type (%s)",
                        earliestSupported, v
                        ),
                v.compareTo(earliestSupported) >= 0);
    }
}
