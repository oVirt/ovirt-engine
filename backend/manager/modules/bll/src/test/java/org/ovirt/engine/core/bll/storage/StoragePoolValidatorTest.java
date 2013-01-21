package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.MockConfigRule;

public class StoragePoolValidatorTest {

    @ClassRule
    public static MockConfigRule mce = new MockConfigRule
            (mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_1.toString(), true),
                    mockConfig(ConfigValues.PosixStorageEnabled, Version.v3_0.toString(), false),
                    mockConfig(ConfigValues.PosixStorageEnabled, Version.v2_2.toString(), false),
                    mockConfig(ConfigValues.PosixStorageEnabled, "general", false));

    private StoragePoolValidator validator = null;
    private storage_pool storagePool = null;

    @Before
    public void setup() {
        storagePool =
                new storage_pool("test",
                        Guid.NewGuid(),
                        "test",
                        StorageType.UNKNOWN.getValue(),
                        StoragePoolStatus.Up.getValue());
        validator = spy(new StoragePoolValidator(storagePool));
    }

    @Test
    public void testPosixDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(Version.v3_1);
        storagePool.setstorage_pool_type(StorageType.POSIXFS);
        assertTrue(validator.isPosixDcAndMatchingCompatiblityVersion().isValid());
    }

    @Test
    public void testPosixDcAndNotMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(Version.v3_0);
        storagePool.setstorage_pool_type(StorageType.POSIXFS);
        ValidationResult result = validator.isPosixDcAndMatchingCompatiblityVersion();
        assertFalse(result.isValid());
        assertMessage(result, VdcBllMessages.DATA_CENTER_POSIX_STORAGE_NOT_SUPPORTED_IN_CURRENT_VERSION);
    }

    @Test
    public void testLocalDcAndMatchingCompatiblityVersion() {
        storagePool.setcompatibility_version(Version.v3_0);
        storagePool.setstorage_pool_type(StorageType.LOCALFS);
        assertTrue(validator.isPosixDcAndMatchingCompatiblityVersion().isValid());
    }

    @Test
    public void testIsNotLocalFsWithDefaultCluster() {
        storagePool.setstorage_pool_type(StorageType.LOCALFS);
        doReturn(false).when(validator).containsDefaultCluster();
        assertTrue(validator.isNotLocalfsWithDefaultCluster().isValid());
    }

    @Test
    public void testIsNotLocalFsWithDefaultClusterWhenClusterIsDefault() {
        storagePool.setstorage_pool_type(StorageType.LOCALFS);
        doReturn(true).when(validator).containsDefaultCluster();
        ValidationResult result = validator.isNotLocalfsWithDefaultCluster();
        assertFalse(result.isValid());
        assertMessage(result, VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_WITH_DEFAULT_VDS_GROUP_CANNOT_BE_LOCALFS);
    }

    private static void assertMessage(ValidationResult result, VdcBllMessages bllMsg) {
        assertEquals("Wrong canDoAction message is returned", bllMsg, result.getMessage());
    }

}
