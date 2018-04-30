package org.ovirt.engine.core.bll;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.utils.InjectedMock;

public class MacPoolValidatorTest extends BaseCommandTest {

    private final MacPool macPool = new MacPool();

    private MacPoolValidator macPoolValidator;

    @Mock
    @InjectedMock
    public MacPoolDao macPoolDaoMock;

    @Mock
    @InjectedMock
    public ClusterDao clusterDao;

    @BeforeEach
    public void setUp() {
        this.macPoolValidator = createMacPoolValidator(macPool);
    }

    private MacPoolValidator createMacPoolValidator(MacPool macPool) {
        MacPoolValidator macPoolValidator = spy(new MacPoolValidator(macPool));
        return macPoolValidator;
    }

    @Test
    public void testDefaultPoolFlagIsNotSetValidUsage() {
        macPool.setDefaultPool(false);
        assertThat(macPoolValidator.defaultPoolFlagIsNotSet(),
                isValid());
    }

    @Test
    public void testDefaultPoolFlagIsNotSetInvalidUsage() {
        macPool.setDefaultPool(true);
        assertThat(macPoolValidator.defaultPoolFlagIsNotSet(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_SETTING_DEFAULT_MAC_POOL_IS_NOT_SUPPORTED));
    }

    @Test
    public void testHasUniqueNameUpdateNotChangingName() {
        final Guid macPoolId = Guid.newGuid();
        final String poolName = "macPool1";

        assertThat(callHasUniqueName(macPoolId, macPoolId, poolName, poolName),
                isValid());
    }

    @Test
    public void testHasUniqueNameRenamingPool() {
        final Guid macPoolId = Guid.newGuid();

        assertThat(callHasUniqueName(macPoolId, macPoolId, "macPool1", "macPool2"),
                isValid());
    }

    @Test
    public void testHasUniqueNameUsingExistingName() {
        final String macPoolName = "macPool1";

        assertThat(callHasUniqueName(Guid.newGuid(), Guid.newGuid(), macPoolName, macPoolName),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED));
    }

    @Test
    public void testHasUniqueNamePersistingNewRecord() {
        assertThat(callHasUniqueName(Guid.newGuid(), Guid.newGuid(), "macPool1", "macPool2"),
                isValid());
    }

    @Test
    public void testHasUniqueNamePersistingNewRecordWithNullId() {
        assertThat(callHasUniqueName(Guid.newGuid(), null, "macPool1", "whatever"),
                isValid());
    }

    @Test
    public void testHasUniqueNamePersistingNewRecordWithNullIdAndSameName() {
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
        when(macPoolDaoMock.getAll()).thenReturn(Collections.singletonList(existingMacPool));

        macPool.setId(macPool2Id);
        macPool.setName(macPool2Name);
        return macPoolValidator.hasUniqueName();
    }

    @Test
    public void testNotRemovingDefaultPool() {
        macPool.setDefaultPool(true);
        assertThat(macPoolValidator.notRemovingDefaultPool(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_DEFAULT_MAC_POOL));
    }

    @Test
    public void testNotRemovingDefaultPoolNonDefaultIsRemoved() {
        assertThat(macPoolValidator.notRemovingDefaultPool(), isValid());
    }

    @Test
    public void testNotRemovingUsedPoolRecordIsUsed() {
        macPool.setId(Guid.newGuid());
        final Cluster cluster = new Cluster();
        cluster.setName("cluster");
        when(clusterDao.getAllClustersByMacPoolId(macPool.getId()))
                .thenReturn(Collections.singletonList(cluster));

        assertThat(macPoolValidator.notRemovingUsedPool(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_STILL_USED_MAC_POOL));
    }

    @Test
    public void testNotRemovingUsedPoolRecordNotUsed() {
        macPool.setId(Guid.newGuid());

        assertThat(macPoolValidator.notRemovingUsedPool(), isValid());
    }

    @Test
    public void testMacPoolExistsEntityNotExist() {
        assertThat(createMacPoolValidator(null).macPoolExists(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_MAC_POOL_DOES_NOT_EXIST));
    }

    @Test
    public void testMacPoolExistsEntityDoesExist() {
        assertThat(macPoolValidator.macPoolExists(), isValid());
    }
}
