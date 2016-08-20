package org.ovirt.engine.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.compat.Version;

@RunWith(Parameterized.class)
public class OSParserTest {

    private Version version;
    private String name;
    private String fullVersion;

    public OSParserTest(String name, int major, int minor, int release, int build, String fullVersion) {
        this.name = name;
        this.version = new Version(major, minor, release, build);
        this.fullVersion = fullVersion;
    }

    @Test
    public void verifyOsFormatCanBeParsed() {
        final OS validOs = OS.fromPackageVersionString(name);
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion()).isEqualTo(version);
        assertThat(validOs.getFullVersion()).isEqualTo(fullVersion);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> namesParams() {

        return Arrays.asList(new Object[][] {
                { "RHEL - 7.2 - 9.el7", 7, 2, -1, -1, "7.2 - 9.el7" },
                { "RHEL - 7 - 1.1503.el7.centos.2.8", 7, -1, -1, -1, "7 - 1.1503.el7.centos.2.8" },
                { "oVirt Node - 3.6 - 0.999.201608161021.el7.centos", 3, 6, -1, -1,
                        "3.6 - 0.999.201608161021.el7.centos" },
                { "RHEV Hypervisor - 7.2 - 20160711.0.el7ev", 7, 2, -1, -1, "7.2 - 20160711.0.el7ev" },
                { "Fedora - 19 - 1", 19, 1, -1, -1, "19 - 1" },
        });
    }
}
