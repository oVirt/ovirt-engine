package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class MacPoolPerClusterTest extends BaseCommandTest {
    private static final String SESSION_ID = "session id";

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private MacPoolDao macPoolDao;

    @Mock
    private DecoratedMacPoolFactory decoratedMacPoolFactory;

    @Mock
    @InjectedMock
    public AuditLogDirector auditLogDirector;

    @Mock
    private MacsUsedAcrossWholeSystem macsUsedAcrossWholeSystem;

    @InjectMocks
    private MacPoolFactory macPoolFactory;

    private MacPool macPool;
    private Cluster cluster;
    private VmNic vmNic;
    private static final String MAC_FROM = "00:1a:4a:15:c0:00";
    private static final String MAC_TO = "00:1a:4a:15:c0:ff";
    private MacPoolPerCluster macPoolPerCluster;
    private CommandContext commandContext;

    @BeforeEach
    public void setUp() {
        commandContext = CommandContext.createContext(SESSION_ID);
        macPool = createMacPool(MAC_FROM, MAC_TO);
        cluster = createCluster(macPool);
        vmNic = createVmNic();

        when(decoratedMacPoolFactory.createDecoratedPool(any(), any())).thenAnswer(AdditionalAnswers.returnsFirstArg());

        mockUsedMacsInSystem(macPool.getId());
        macPoolPerCluster = new MacPoolPerCluster(macPoolDao,
                clusterDao,
                macPoolFactory,
                decoratedMacPoolFactory);
    }

    @Test
    public void testPoolDoesNotExistForGivenCluster() {
        macPoolPerCluster.initialize();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> getMacPool(cluster.getId()));
        assertEquals(macPoolPerCluster.createExceptionMessageMacPoolHavingIdDoesNotExist(null), e.getMessage());
    }

    @Test
    public void testPoolOfGivenGuidExist() {
        mockCluster(cluster);
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();
        assertThat(getMacPool(cluster.getId()), is(notNullValue()));
    }

    @Test
    public void testNicIsCorrectlyAllocatedInScopedPool() {
        mockCluster(cluster);
        mockGettingAllMacPools(macPool);
        mockUsedMacsInSystem(macPool.getId(), vmNic.getMacAddress());

        macPoolPerCluster.initialize();
        assertThat("scoped pool for this nic should exist",
                getMacPool(cluster.getId()), is(notNullValue()));

        assertThat("provided mac should be used in returned pool",
                getMacPool(cluster.getId()).isMacInUse(vmNic.getMacAddress()), is(true));
    }

    @Test
    public void testCreatePool() {
        macPoolPerCluster.initialize();

        mockCluster(cluster);
        macPoolPerCluster.createPool(macPool);
        assertThat("scoped pool for this data center should exist",
                getMacPool(cluster.getId()), is (notNullValue()));
    }

    @Test
    public void testCreatePoolWhichExists() {
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> macPoolPerCluster.createPool(macPool));
        assertEquals(MacPoolPerCluster.UNABLE_TO_CREATE_MAC_POOL_IT_ALREADY_EXIST, e.getMessage());
    }

    @Test
    public void testModifyOfExistingMacPool() {
        final String macAddress1 = "00:00:00:00:00:01";
        final String macAddress2 = "00:00:00:00:00:02";

        MacPool macPool = createMacPool(macAddress1, macAddress1);
        Cluster cluster = createCluster(macPool);

        mockCluster(cluster);
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();
        macPoolPerCluster.getMacPoolById(macPool.getId()).getMacsStorage().setSkipAllocationPredicate(any -> false);

        assertThat(getMacPool(cluster.getId()).addMac(MAC_FROM), is(true));
        assertThat(getMacPool(cluster.getId()).addMac(MAC_FROM), is(false));

        final String allocatedMac = allocateMac(cluster);

        /*needed due to implementation of modifyPool;
        modify assumes, that all allocated macs is used for vmNics. If allocatedMac succeeded it's expected that all
        vmNics were also successfully persisted to db or all allocated macs were returned to the pool. So after
        allocation we need to mock db, otherwise re-init in modifyPool would return improper results.*/
        mockUsedMacsInSystem(getMacPool(cluster.getId()).getId(), allocatedMac, MAC_FROM);

        assertThat(allocatedMac, is(macAddress1));
        assertThrows(
                EngineException.class,
                () -> allocateMac(cluster),
                "This allocation should not succeed, MAC should be full.");

        macPool.setAllowDuplicateMacAddresses(true);
        final MacRange macRange = new MacRange();
        macRange.setMacFrom(macAddress1);
        macRange.setMacTo(macAddress2);

        macPool.setRanges(Collections.singletonList(macRange));
        macPoolPerCluster.modifyPool(macPool);

        assertThat(getMacPool(cluster.getId()).addMac(MAC_FROM), is(true));
        assertThat(allocateMac(cluster), is(macAddress2));
    }

    protected String allocateMac(Cluster cluster) {
        final Guid clusterId = cluster.getId();
        return getMacPool(clusterId).allocateNewMac();
    }

    private org.ovirt.engine.core.bll.network.macpool.MacPool getMacPool(Guid clusterId) {
        return macPoolPerCluster.getMacPoolForCluster(clusterId, commandContext);
    }

    @Test
    public void testModifyOfNotExistingMacPool() {
        macPoolPerCluster.initialize();

        MacPool macPool = createMacPool(null, null);
        Guid macPoolId = macPool.getId();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> macPoolPerCluster.modifyPool(macPool));
        assertEquals(macPoolPerCluster.createExceptionMessageMacPoolHavingIdDoesNotExist(macPoolId), e.getMessage());
    }

    @Test
    public void testRemoveOfMacPool() {
        mockCluster(cluster);
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();

        assertThat(getMacPool(cluster.getId()), is(notNullValue()));

        Guid macPoolId = macPool.getId();
        macPoolPerCluster.removePool(macPoolId);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> getMacPool(cluster.getId()));
        assertEquals(macPoolPerCluster.createExceptionMessageMacPoolHavingIdDoesNotExist(macPoolId), e.getMessage());
    }

    @Test
    public void testRemoveOfInexistentMacPool() {
        macPoolPerCluster.initialize();

        assertThrows(
                IllegalStateException.class,
                () -> getMacPool(cluster.getId()),
                "pool for given data center should not exist");

        macPoolPerCluster.removePool(macPool.getId());
        //nothing to test, should not fail.
    }

    private Cluster createCluster(MacPool macPool) {

        //mock existing data centers.
        final Cluster cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setMacPoolId(macPool.getId());
        return cluster;
    }

    private MacPool createMacPool(String macFrom, String macTo) {
        final MacRange macRange = new MacRange();
        macRange.setMacFrom(macFrom);
        macRange.setMacTo(macTo);

        final MacPool macPool = new MacPool();
        macPool.setId(Guid.newGuid());
        macPool.setRanges(Collections.singletonList(macRange));
        return macPool;
    }

    protected void mockUsedMacsInSystem(Guid macPoolId, String... macAddress) {
        when(macsUsedAcrossWholeSystem.getMacsForMacPool(macPoolId)).thenReturn(Arrays.asList(macAddress));
    }

    protected void mockGettingAllMacPools(MacPool... macPool) {
        when(macPoolDao.getAll()).thenReturn(Arrays.asList(macPool));
    }

    protected VmNic createVmNic() {
        final VmNic vmNic = new VmNic();
        vmNic.setMacAddress("00:1a:4a:15:c0:fe");
        return vmNic;
    }

    protected void mockCluster(Cluster cluster) {
        when(clusterDao.get(eq(cluster.getId()))).thenReturn(cluster);
    }
}
