package org.ovirt.engine.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.compat.Version;

public class OSParserTest {
    @ParameterizedTest
    @MethodSource
    public void verifyOsFormatCanBeParsed
            (String name, int major, int minor, int release, int build, String fullVersion) {

        final Version version = new Version(major, minor, release, build);
        final OS validOs = OS.fromPackageVersionString(name);
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion()).isEqualTo(version);
        assertThat(validOs.getFullVersion()).isEqualTo(fullVersion);
    }

    public static Stream<Arguments> verifyOsFormatCanBeParsed() {
        return Stream.of(
                Arguments.of("RHEL - 7.2 - 9.el7", 7, 2, -1, -1, "7.2 - 9.el7"),
                Arguments.of("RHEL - 7 - 1.1503.el7.centos.2.8", 7, -1, -1, -1, "7 - 1.1503.el7.centos.2.8"),
                Arguments.of("oVirt Node - 3.6 - 0.999.201608161021.el7.centos", 3, 6, -1, -1,
                        "3.6 - 0.999.201608161021.el7.centos"),
                Arguments.of("RHEV Hypervisor - 7.2 - 20160711.0.el7ev", 7, 2, -1, -1, "7.2 - 20160711.0.el7ev"),
                Arguments.of("Fedora - 19 - 1", 19, 1, -1, -1, "19 - 1")
        );
    }
}
