package org.ovirt.engine.core.bll.scheduling;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.compat.Version;

@RunWith(MockitoJUnitRunner.class)
public class OSTest {

    @Test
    public void shouldDetectVersionWithoutDigits() {
        final OS invalidOs = OS.fromPackageVersionString("Centos - Seven - release");
        assertThat(invalidOs.isValid(), is(false));
    }

    @Test
    public void shouldHandleAlNumMajorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7Server - release");
        assertThat(validOs.isValid(), is(true));
        assertThat(validOs.getVersion().getMajor(), equalTo(7));
    }

    @Test
    public void shouldHandleAlNumFullVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7.3.2Server - release");
        assertThat(validOs.isValid(), is(true));
        assertThat(validOs.getVersion().getMajor(), equalTo(7));
        assertThat(validOs.getVersion().getMinor(), equalTo(3));
        assertThat(validOs.getVersion().getBuild(), equalTo(2));
    }

    @Test
    public void shouldHandleNumMajorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7 - release");
        assertThat(validOs.isValid(), is(true));
        assertThat(validOs.getVersion().getMajor(), equalTo(7));
        assertThat(validOs.getVersion().getMinor(), equalTo(-1));
    }

    @Test
    public void shouldHandleNumMinorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7.3 - release");
        assertThat(validOs.isValid(), is(true));
        assertThat(validOs.getVersion().getMajor(), equalTo(7));
        assertThat(validOs.getVersion().getMinor(), equalTo(3));
    }

    @Test
    public void shouldHandleStrangeMajorVersion() {
        final OS validOs = OS.fromPackageVersionString("Centos - 7. - release");
        assertThat(validOs.isValid(), is(true));
        assertThat(validOs.getVersion().getMajor(), equalTo(7));
        assertThat(validOs.getVersion().getMinor(), equalTo(-1));
    }

    @Test
    public void shouldDetectIncompleteOsIdentifier() {
        final OS invalidOs = OS.fromPackageVersionString("Centos");
        assertThat(invalidOs.isValid(), is(false));
    }

    @Test
    public void shouldOnlyTakeStartingDigits() {
        final OS invalidOs = OS.fromPackageVersionString("Centos - Server7 - release");
        assertThat(invalidOs.isValid(), is(false));
    }

    @Test
    public void shouldFalbackToEl6ReleaseIdentifier() {
        final OS validOs = OS.fromPackageVersionString("Centos - Server7 - release.el6");
        assertThat(validOs.isValid(), is(true));
        assertThat(validOs.getVersion().getMajor(), equalTo(6));
    }

    @Test
    public void shouldFalbackToEl7ReleaseIdentifier() {
        final OS validOs = OS.fromPackageVersionString("Centos - Server9 - release.el7");
        assertThat(validOs.isValid(), is(true));
        assertThat(validOs.getVersion().getMajor(), equalTo(7));
    }

    @Test
    public void shouldDetectSameMajorVersion() {
        assertThat(new OS("test", new Version("6.3")).isSameMajorVersion(new OS("test", new Version("6.6"))), is(true));
    }

    @Test
    public void shouldDetectDifferentMajorVersion() {
        assertThat(new OS("test", new Version("6.3")).isSameMajorVersion(new OS("test", new Version("7.3"))), is(false));
    }

    @Test
    public void shouldDetectDifferentOs() {
        assertThat(new OS("os1", new Version("6.3")).isSameOsFamily(new OS("os2", new Version("6.3"))), is(false));
    }

    @Test
    public void shouldDetectSameOs() {
        assertThat(new OS("os1", new Version("6.3")).isSameOsFamily(new OS("os1", new Version("6.3"))), is(true));
    }

    @Test
    public void shouldDetectEqualOs() {
        assertThat(new OS("RHEL", new Version("6.3"))
                .isSameOsFamily(new OS("oVirt Node", new Version("6.3"))), is(true));
        assertThat(new OS("RHEL", new Version("6.3"))
                .isSameOsFamily(new OS("RHEV Hypervisor", new Version("6.3"))), is(true));
        assertThat(new OS("oVirt Node", new Version("6.3"))
                .isSameOsFamily(new OS("RHEV Hypervisor", new Version("6.3"))), is(true));
    }

    @Test
    public void shouldReturnCorrectOsFamiliy() {
        assertThat(new OS("RHEV Hypervisor", new Version("6.3")).getOsFamily(), equalTo("RHEL"));
        assertThat(new OS("RHEL", new Version("6.3")).getOsFamily(), equalTo("RHEL"));
        assertThat(new OS("oVirt Node", new Version("6.3")).getOsFamily(), equalTo("RHEL"));
    }

    @Test
    public void shouldDetectNewerOs() {
        assertThat(new OS("os1", new Version("6.3")).isNewerThan(new OS("os1", new Version("6.2"))), is(true));
        assertThat(new OS("os1", new Version("7.1")).isNewerThan(new OS("os1", new Version("6.2"))), is(true));
        assertThat(new OS("os1", new Version("6.2")).isNewerThan(new OS("os1", new Version("6.3"))), is(false));
        assertThat(new OS("os1", new Version("5.4")).isNewerThan(new OS("os1", new Version("6.3"))), is(false));
    }

    @Test
    public void shouldDetectOlderOs() {
        assertThat(new OS("os1", new Version("6.2")).isOlderThan(new OS("os1", new Version("6.3"))), is(true));
        assertThat(new OS("os1", new Version("6.2")).isOlderThan(new OS("os1", new Version("7.1"))), is(true));
        assertThat(new OS("os1", new Version("6.2")).isOlderThan(new OS("os1", new Version("6.1"))), is(false));
        assertThat(new OS("os1", new Version("7.2")).isOlderThan(new OS("os1", new Version("6.3"))), is(false));
    }
}
