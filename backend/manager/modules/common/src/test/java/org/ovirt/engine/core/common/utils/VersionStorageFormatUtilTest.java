package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Version;

/**
 * A test case for {@link VersionStorageFormatUtil}. This test was put in place to ensure that no new entries would be
 * added to {@link Version#ALL} without {@link VersionStorageFormatUtil} being updated.
 */
public class VersionStorageFormatUtilTest {
    public static Stream<Version> versionHasMatchingFormat() {
        return Version.ALL.stream();
    }

    @ParameterizedTest
    @MethodSource
    public void versionHasMatchingFormat(Version v) {
        StorageFormatType sft = VersionStorageFormatUtil.getForVersion(v);
        assertNotNull(sft, String.format("Missing format for version %s", v));

        Version earliestSupported = VersionStorageFormatUtil.getEarliestVersionSupported(sft);
        assertNotNull(earliestSupported, String.format("Missing earliest version for format %s", sft));

        assertTrue(v.compareTo(earliestSupported) >= 0,
                String.format(
                        "Earliest supported version (%s) should no be later than the version requiring this type (%s)",
                        earliestSupported, v
                ));
    }
}
