package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

@RunWith(MockitoJUnitRunner.class)
public class MacPoolValidatorTest extends DbDependentTestBase {

    private final MacPool macPool = new MacPool();

    private MacPoolValidator macPoolValidator;

    @Mock
    private MacPoolDao macPoolDaoMock;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Before
    public void setUp() throws Exception {
        this.macPoolValidator = createMacPoolValidator(macPool);
        when(DbFacade.getInstance().getMacPoolDao()).thenReturn(macPoolDaoMock);
        when(DbFacade.getInstance().getStoragePoolDao()).thenReturn(storagePoolDao);
    }

    private MacPoolValidator createMacPoolValidator(MacPool macPool) {
        MacPoolValidator macPoolValidator = spy(new MacPoolValidator(macPool));
        return macPoolValidator;
    }

    @Test
    public void testDefaultPoolFlagIsNotSetValidUsage() throws Exception {
        macPool.setDefaultPool(false);
        assertThat(macPoolValidator.defaultPoolFlagIsNotSet(),
                isValid());
    }

    @Test
    public void testDefaultPoolFlagIsNotSetInvalidUsage() throws Exception {
        macPool.setDefaultPool(true);
        assertThat(macPoolValidator.defaultPoolFlagIsNotSet(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_SETTING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED));
    }

    @Test
    public void testHasUniqueNameUpdateNotChangingName() throws Exception {
        final Guid macPoolId = Guid.newGuid();
        final String poolName = "macPool1";

        assertThat(callHasUniqueName(macPoolId, macPoolId, poolName, poolName),
                isValid());
    }

    @Test
    public void testHasUniqueNameRenamingPool() throws Exception {
        final Guid macPoolId = Guid.newGuid();

        assertThat(callHasUniqueName(macPoolId, macPoolId, "macPool1", "macPool2"),
                isValid());
    }

    @Test
    public void testHasUniqueNameUsingExistingName() throws Exception {
        final String macPoolName = "macPool1";

        assertThat(callHasUniqueName(Guid.newGuid(), Guid.newGuid(), macPoolName, macPoolName),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    public void testHasUniqueNamePersistingNewRecord() throws Exception {
        assertThat(callHasUniqueName(Guid.newGuid(), Guid.newGuid(), "macPool1", "macPool2"),
                isValid());
    }

    @Test
    public void testHasUniqueNamePersistingNewRecordWithNullId() throws Exception {
        assertThat(callHasUniqueName(Guid.newGuid(), null, "macPool1", "whatever"),
                isValid());
    }

    @Test
    public void testHasUniqueNamePersistingNewRecordWithNullIdAndSameName() throws Exception {
        final String macPoolName = "macPool1";

        assertThat(callHasUniqueName(Guid.newGuid(), null, macPoolName, macPoolName),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    private ValidationResult callHasUniqueName(Guid macPool1Id,
            Guid macPool2Id,
            String macPool1Name,
            String macPool2Name) {

        final MacPool existingMacPool = new MacPool();
        existingMacPool.setId(macPool1Id);
        existingMacPool.setName(macPool1Name);
        when(macPoolDaoMock.getAll()).thenReturn(Arrays.asList(existingMacPool));

        macPool.setId(macPool2Id);
        macPool.setName(macPool2Name);
        return macPoolValidator.hasUniqueName();
    }

    @Test
    public void testNotRemovingDefaultPool() throws Exception {
        macPool.setDefaultPool(true);
        assertThat(macPoolValidator.notRemovingDefaultPool(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_DEFAULT_MAC_POOL));
    }

    @Test
    public void testNotRemovingDefaultPoolNonDefaultIsRemoved() throws Exception {
        assertThat(macPoolValidator.notRemovingDefaultPool(), isValid());
    }

    @Test
    public void testNotRemovingUsedPoolRecordIsUsed() throws Exception {
        macPool.setId(Guid.newGuid());
        final StoragePool storagePool = new StoragePool();
        storagePool.setName("storagePool");
        when(storagePoolDao.getAllDataCentersByMacPoolId(macPool.getId()))
                .thenReturn(Collections.singletonList(storagePool));

        assertThat(macPoolValidator.notRemovingUsedPool(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_STILL_USED_MAC_POOL));
    }

    @Test
    public void testNotRemovingUsedPoolRecordNotUsed() throws Exception {
        macPool.setId(Guid.newGuid());

        when(storagePoolDao.getAllDataCentersByMacPoolId(macPool.getId()))
                .thenReturn(Collections.<StoragePool>emptyList());

        assertThat(macPoolValidator.notRemovingUsedPool(), isValid());
    }

    @Test
    public void testMacPoolExistsEntityNotExist() throws Exception {
        assertThat(createMacPoolValidator(null).macPoolExists(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAC_POOL_DOES_NOT_EXIST));
    }

    @Test
    public void testMacPoolExistsEntityDoesExist() throws Exception {
        assertThat(macPoolValidator.macPoolExists(), isValid());
    }
}
