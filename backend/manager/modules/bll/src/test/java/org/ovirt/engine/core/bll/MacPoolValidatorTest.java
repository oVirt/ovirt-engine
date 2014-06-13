package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeLocator;
import org.ovirt.engine.core.dao.MacPoolDao;

@RunWith(MockitoJUnitRunner.class)
public class MacPoolValidatorTest {

    private MacPool macPool;

    @Mock
    private MacPoolDao macPoolDaoMock;

    @Before
    public void setUp() throws Exception {
        macPool = new MacPool();

        final DbFacade dbFacadeMock = mock(DbFacade.class);
        DbFacadeLocator.setDbFacade(dbFacadeMock);

        when(dbFacadeMock.getMacPoolDao()).thenReturn(macPoolDaoMock);
    }

    @Test
    public void testDefaultPoolFlagIsNotSetValidUsage() throws Exception {
        macPool.setDefaultPool(false);
        assertThat(new MacPoolValidator(macPool).defaultPoolFlagIsNotSet(),
                isValid());
    }

    @Test
    public void testDefaultPoolFlagIsNotSetInvalidUsage() throws Exception {
        macPool.setDefaultPool(true);
        assertThat(new MacPoolValidator(macPool).defaultPoolFlagIsNotSet(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_SETTING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED));
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    private ValidationResult callHasUniqueName(Guid macPool1Id,
            Guid macPool2Id,
            String macPool1Name,
            String macPool2Name) {
        when(macPoolDaoMock.getAll()).thenReturn(Arrays.asList(createMacPool(macPool1Id, macPool1Name)));

        return new MacPoolValidator(createMacPool(macPool2Id, macPool2Name)).hasUniqueName();
    }

    private MacPool createMacPool(Guid macPool1Id, String macPool1Name) {
        final MacPool macPool = new MacPool();
        macPool.setName(macPool1Name);
        macPool.setId(macPool1Id);
        return macPool;
    }

    @Test
    public void testNotRemovingDefaultPool() throws Exception {
        macPool.setDefaultPool(true);
        assertThat(new MacPoolValidator(macPool).notRemovingDefaultPool(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_DEFAULT_MAC_POOL));
    }

    @Test
    public void testNotRemovingDefaultPoolNonDefaultIsRemoved() throws Exception {
        assertThat(new MacPoolValidator(macPool).notRemovingDefaultPool(), isValid());
    }

    @Test
    public void testNotRemovingUsedPoolRecordIsUsed() throws Exception {
        macPool.setId(Guid.newGuid());
        when(macPoolDaoMock.getDcUsageCount(eq(macPool.getId()))).thenReturn(1);

        assertThat(new MacPoolValidator(macPool).notRemovingUsedPool(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_STILL_USED_MAC_POOL));
    }

    @Test
    public void testNotRemovingUsedPoolRecordNotUsed() throws Exception {
        macPool.setId(Guid.newGuid());
        when(macPoolDaoMock.getDcUsageCount(eq(macPool.getId()))).thenReturn(0);

        assertThat(new MacPoolValidator(macPool).notRemovingUsedPool(), isValid());
    }

    @Test
    public void testMacPoolExistsEntityNotExist() throws Exception {
        assertThat(new MacPoolValidator(null).macPoolExists(),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_MAC_POOL_DOES_NOT_EXIST));
    }

    @Test
    public void testMacPoolExistsEntityDoesExist() throws Exception {
        assertThat(new MacPoolValidator(macPool).macPoolExists(), isValid());
    }
}
