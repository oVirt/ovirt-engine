package org.ovirt.engine.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.compat.Version;

public class OSTest {

    @Test
    public void shouldDetectVersionWithoutDigits() {
        final OS invalidOs = OS.fromPackageVersionString("Centos - Seven - release");
        assertThat(invalidOs.isValid()).isFalse();
    }

    @Test
    public void shouldHandleAlNumMajorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7Server - release");
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion().getMajor()).isEqualTo(7);
    }

    @Test
    public void shouldHandleAlNumFullVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7.3.2Server - release");
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion().getMajor()).isEqualTo(7);
        assertThat(validOs.getVersion().getMinor()).isEqualTo(3);
        assertThat(validOs.getVersion().getBuild()).isEqualTo(2);
    }

    @Test
    public void shouldHandleNumMajorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7 - release");
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion().getMajor()).isEqualTo(7);
        assertThat(validOs.getVersion().getMinor()).isEqualTo(-1);
    }

    @Test
    public void shouldHandleNumMinorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7.3 - release");
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion().getMajor()).isEqualTo(7);
        assertThat(validOs.getVersion().getMinor()).isEqualTo(3);
    }

    @Test
    public void shouldHandleStrangeMajorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7. - release");
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion().getMajor()).isEqualTo(7);
        assertThat(validOs.getVersion().getMinor()).isEqualTo(-1);
    }

    @Test
    public void shouldDetectIncompleteOsIdentifier() {
        final OS invalidOs = OS.fromPackageVersionString("Centos");
        assertThat(invalidOs.isValid()).isFalse();
    }

    @Test
    public void shouldOnlyTakeStartingDigits() {
        final OS invalidOs = OS.fromPackageVersionString("Centos - Server7 - release");
        assertThat(invalidOs.isValid()).isFalse();
    }

    @Test
    public void shouldFalbackToEl6ReleaseIdentifier() {
        final OS validOs = OS.fromPackageVersionString("Centos - Server7 - release.el6");
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion().getMajor()).isEqualTo(6);
    }

    @Test
    public void shouldFalbackToEl7ReleaseIdentifier() {
        final OS validOs = OS.fromPackageVersionString("Centos - Server9 - release.el7");
        assertThat(validOs.isValid()).isTrue();
        assertThat(validOs.getVersion().getMajor()).isEqualTo(7);
    }

    @Test
    public void shouldDetectSameMajorVersion() {
        assertThat(new OS("test", new Version("6.3"), "").isSameMajorVersion(new OS("test", new Version("6.6"), "")))
                .isTrue();
    }

    @Test
    public void shouldDetectDifferentMajorVersion() {
        assertThat(new OS("test", new Version("6.3"), "").isSameMajorVersion(new OS("test", new Version("7.3"), "")))
                .isFalse();
    }

    @Test
    public void shouldDetectDifferentOs() {
        assertThat(new OS("os1", new Version("6.3"), "").isSameOsFamily(new OS("os2", new Version("6.3"), ""))).isFalse();
    }

    @Test
    public void shouldDetectSameOs() {
        assertThat(new OS("os1", new Version("6.3"), "").isSameOsFamily(new OS("os1", new Version("6.3"), ""))).isTrue();
    }

    @Test
    public void shouldDetectEqualOs() {
        assertThat(new OS("RHEL", new Version("6.3"), "")
                .isSameOsFamily(new OS("oVirt Node", new Version("6.3"), ""))).isTrue();
        assertThat(new OS("RHEL", new Version("6.3"), "")
                .isSameOsFamily(new OS("RHEV Hypervisor", new Version("6.3"), ""))).isTrue();
        assertThat(new OS("oVirt Node", new Version("6.3"), "")
                .isSameOsFamily(new OS("RHEV Hypervisor", new Version("6.3"), ""))).isTrue();
    }

    @Test
    public void shouldReturnCorrectOsFamiliy() {
        assertThat(new OS("RHEV Hypervisor", new Version("6.3"), "").getOsFamily()).isEqualTo("RHEL");
        assertThat(new OS("RHEL", new Version("6.3"), "").getOsFamily()).isEqualTo("RHEL");
        assertThat(new OS("oVirt Node", new Version("6.3"), "").getOsFamily()).isEqualTo("RHEL");
    }

    @Test
    public void shouldDetectNewerOs() {
        assertThat(new OS("os1", new Version("6.3"), "").isNewerThan(new OS("os1", new Version("6.2"), ""))).isTrue();
        assertThat(new OS("os1", new Version("7.1"), "").isNewerThan(new OS("os1", new Version("6.2"), ""))).isTrue();
        assertThat(new OS("os1", new Version("6.2"), "").isNewerThan(new OS("os1", new Version("6.3"), ""))).isFalse();
        assertThat(new OS("os1", new Version("5.4"), "").isNewerThan(new OS("os1", new Version("6.3"), ""))).isFalse();
    }

    @Test
    public void shouldDetectOlderOs() {
        assertThat(new OS("os1", new Version("6.2"), "").isOlderThan(new OS("os1", new Version("6.3"), ""))).isTrue();
        assertThat(new OS("os1", new Version("6.2"), "").isOlderThan(new OS("os1", new Version("7.1"), ""))).isTrue();
        assertThat(new OS("os1", new Version("6.2"), "").isOlderThan(new OS("os1", new Version("6.1"), ""))).isFalse();
        assertThat(new OS("os1", new Version("7.2"), "").isOlderThan(new OS("os1", new Version("6.3"), ""))).isFalse();
    }
}
