package org.ovirt.engine.api.restapi.utils;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.compat.Version;

public class VersionUtilsTest {
    @Test
    public void greaterOrEqual() {
        Assert.assertTrue(VersionUtils.greaterOrEqual(new Version(1, 1), new Version(1, 0)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(new Version(1, 1), new Version(1, 1)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(new Version(1, 1, 1), new Version(1, 1)));
        // an integer beyond the ones cached in Integer
        Assert.assertTrue(VersionUtils.greaterOrEqual(new Version(6789, 9876, 1), new Version(6789, 6789)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(new Version("2"), new Version(1, 1)));

        Assert.assertFalse(VersionUtils.greaterOrEqual(new Version(1, 0), new Version(2, 0)));
        Assert.assertFalse(VersionUtils.greaterOrEqual(new Version(1, 1), new Version(2, 0)));

    }

    @Test
    public void greaterOrEqualWithCompat() {
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 0, null, null), new Version(1, 0)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, null, null), new Version(1, 1)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, 1, null), new Version(1, 1)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(6789, 9876, 1, null), new Version(6789, 9876)));
        Assert.assertFalse(VersionUtils.greaterOrEqual(modelVersion(1, 0, null, null), new Version(2, 0)));
        Assert.assertFalse(VersionUtils.greaterOrEqual(modelVersion(1, null, null, null), new Version(2, 0)));
        // note that the major version can not be null
        // Assert.assertFalse(VersionUtils.greaterOrEqual(modelVersion(null, null, null, null), new Version(2, 0)));
    }

    @Test
    public void greaterOrEqualWithModel() {
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 0, null, null), modelVersion(1, 0, null, null)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, null, null), modelVersion(1, 0, null, null)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, null, null), modelVersion(1, 0, null, null)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, null, null), modelVersion(1, 1, null, null)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, 1, null), modelVersion(1, 1, null, null)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, 1, 1), modelVersion(1, 1, null, null)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, null, null), modelVersion(1, 0, 1, 1)));
        Assert.assertTrue(VersionUtils.greaterOrEqual(modelVersion(1, 1, 1, null), modelVersion(1, 0, 1, 1)));
    }

    private org.ovirt.engine.api.model.Version modelVersion(Integer major,
            Integer minor,
            Integer build,
            Integer revision) {
        final org.ovirt.engine.api.model.Version version = new org.ovirt.engine.api.model.Version();
        version.setRevision(revision);
        version.setBuild(build);
        version.setMajor(major);
        version.setMinor(minor);
        return version;
    }
}
