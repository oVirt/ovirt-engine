package org.ovirt.engine.core.bll.network.macpool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.DbDependentTestBase;
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
import org.ovirt.engine.core.di.InjectorRule;

@RunWith(MockitoJUnitRunner.class)
public class MacPoolPerClusterTest extends DbDependentTestBase {
    private static final String SESSION_ID = "session id";

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private MacPoolDao macPoolDao;

    @Mock
    private DecoratedMacPoolFactory decoratedMacPoolFactory;

    @Mock
    private AuditLogDirector auditLogDirector;

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

    @Before
    public void setUp() throws Exception {
        injectorRule.bind(AuditLogDirector.class, auditLogDirector);

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
    public void testPoolDoesNotExistForGivenCluster() throws Exception {
        macPoolPerCluster.initialize();
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(macPoolPerCluster.createExceptionMessageMacPoolHavingIdDoesNotExist(null));
        getMacPool(cluster.getId());
    }

    @Test
    public void testPoolOfGivenGuidExist() {
        mockCluster(cluster);
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();
        assertThat(getMacPool(cluster.getId()), is(notNullValue()));
    }

    @Test
    public void testNicIsCorrectlyAllocatedInScopedPool() throws Exception {
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
    public void testCreatePool() throws Exception {
        macPoolPerCluster.initialize();

        mockCluster(cluster);
        macPoolPerCluster.createPool(macPool);
        assertThat("scoped pool for this data center should exist",
                getMacPool(cluster.getId()), is (notNullValue()));
    }

    @Test
    public void testCreatePoolWhichExists() throws Exception {
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(MacPoolPerCluster.UNABLE_TO_CREATE_MAC_POOL_IT_ALREADY_EXIST);
        macPoolPerCluster.createPool(macPool);
    }

    @Test
    public void testModifyOfExistingMacPool() throws Exception {
        final String macAddress1 = "00:00:00:00:00:01";
        final String macAddress2 = "00:00:00:00:00:02";

        MacPool macPool = createMacPool(macAddress1, macAddress1);
        Cluster cluster = createCluster(macPool);

        mockCluster(cluster);
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();

        assertThat(getMacPool(cluster.getId()).addMac(MAC_FROM), is(true));
        assertThat(getMacPool(cluster.getId()).addMac(MAC_FROM), is(false));

        final String allocatedMac = allocateMac(cluster);

        /*needed due to implementation of modifyPool;
        modify assumes, that all allocated macs is used for vmNics. If allocatedMac succeeded it's expected that all
        vmNics were also successfully persisted to db or all allocated macs were returned to the pool. So after
        allocation we need to mock db, otherwise re-init in modifyPool would return improper results.*/
        mockUsedMacsInSystem(getMacPool(cluster.getId()).getId(), allocatedMac, MAC_FROM);

        assertThat(allocatedMac, is(macAddress1));
        try {
            allocateMac(cluster);
            fail("this allocation should not succeed, MAC should be full.");
        } catch (EngineException e) {
            //ok, this exception should occur.
        }

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
    public void testModifyOfNotExistingMacPool() throws Exception {
        macPoolPerCluster.initialize();

        expectedException.expect(IllegalStateException.class);
        MacPool macPool = createMacPool(null, null);
        Guid macPoolId = macPool.getId();
        expectedException.expectMessage(macPoolPerCluster.createExceptionMessageMacPoolHavingIdDoesNotExist(macPoolId));
        macPoolPerCluster.modifyPool(macPool);
    }

    @Test
    public void testRemoveOfMacPool() throws Exception {
        mockCluster(cluster);
        mockGettingAllMacPools(macPool);
        macPoolPerCluster.initialize();

        assertThat(getMacPool(cluster.getId()), is(notNullValue()));

        Guid macPoolId = macPool.getId();
        macPoolPerCluster.removePool(macPoolId);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(macPoolPerCluster.createExceptionMessageMacPoolHavingIdDoesNotExist(macPoolId));
        getMacPool(cluster.getId());
    }

    @Test
    public void testRemoveOfInexistentMacPool() throws Exception {
        macPoolPerCluster.initialize();

        try {
            getMacPool(cluster.getId());
            fail("pool for given data center should not exist");
        } catch (IllegalStateException e) {
            //ignore this exception.
        }

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
