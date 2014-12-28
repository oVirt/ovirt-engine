package org.ovirt.engine.core.bll.validator.storage;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

public class StoragePoolValidatorTest {

    private static final Version UNSUPPORTED_VERSION = Version.v3_0;
    private static final Version SUPPORTED_VERSION = Version.v3_3;

    @ClassRule
    public static MockConfigRule mce = new MockConfigRule
            (mockConfig(ConfigValues.PosixStorageEnabled, UNSUPPORTED_VERSION.toString(), false),
                    mockConfig(ConfigValues.PosixStorageEnabled, SUPPORTED_VERSION.toString(), true),
                    mockConfig(ConfigValues.GlusterFsStorageEnabled, UNSUPPORTED_VERSION.toString(), false),
                    mockConfig(ConfigValues.GlusterFsStorageEnabled, SUPPORTED_VERSION.toString(), true));

    private StoragePoolValidator validator;
    private StoragePool storagePool;

    @Before
    public void setup() {
        storagePool = new StoragePool();
        storagePool.setStatus(StoragePoolStatus.Up);
        validator = spy(new StoragePoolValidator(storagePool));
    }

    @Test
    public void testPosixDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(SUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isPosixSupportedInDC(), isValid());
    }

    @Test
    public void testPosixDcAndNotMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isPosixSupportedInDC(),
                failsWith(VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testGlusterDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(SUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isGlusterSupportedInDC(), isValid());
    }

    @Test
    public void testGlusterDcAndNotMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(false);
        assertThat(validator.isGlusterSupportedInDC(),
                failsWith(VdcBllMessages.DATA_CENTER_GLUSTER_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION));
    }

    @Test
    public void testLocalDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(UNSUPPORTED_VERSION);
        storagePool.setIsLocal(true);
        assertThat(validator.isPosixSupportedInDC(), isValid());
    }

    @Test
    public void testIsNotLocalFsWithDefaultCluster() {
        storagePool.setIsLocal(true);
        doReturn(false).when(validator).containsDefaultCluster();
        assertThat(validator.isNotLocalfsWithDefaultCluster(), isValid());
    }

    @Test
    public void testIsNotLocalFsWithDefaultClusterWhenClusterIsDefault() {
        storagePool.setIsLocal(true);
        doReturn(true).when(validator).containsDefaultCluster();
        assertThat(validator.isNotLocalfsWithDefaultCluster(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS));
    }

    @Test
    public void testIsUpdValid() {
        assertThat("Storage pool should be up", validator.isUp(), isValid());
    }

    @Test
    public void testIsUpdInvalid() {
        storagePool.setStatus(StoragePoolStatus.NonResponsive);
        assertThat(validator.isUp(), failsWith(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND));
    }
}
