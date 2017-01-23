package org.ovirt.engine.core.bll;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.action.ChangeVMClusterParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.VmNicDao;

@RunWith(MockitoJUnitRunner.class)
public class ChangeVMClusterCommandTest {

    @Mock
    private MoveMacs moveMacs;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private VmNicDao vmNicDao;

    @Mock
    private MacPoolPerCluster macPoolPerCluster;

    @Mock
    private ReadMacPool sourceMacPool;

    @Mock
    private ReadMacPool targetMacPool;

    private final ChangeVMClusterParameters parameters = new ChangeVMClusterParameters();
    private final VM existingVm = createVm();

    @InjectMocks
    private ChangeVMClusterCommand<ChangeVMClusterParameters> underTest =
            new ChangeVMClusterCommand<>(parameters, null);

    @Test
    public void canRunForHostedEngine() throws Exception {
        // given hosted engine VM
        VM hostedEngine = new VM();
        hostedEngine.setOrigin(OriginType.MANAGED_HOSTED_ENGINE);
        underTest.setVm(hostedEngine);

        underTest.init();

        assertThat(underTest.canRunActionOnNonManagedVm(), is(true));
    }

    @Test
    public void testNoChangeWhenClustersDidNotChange() throws Exception {
        Cluster cluster = createCluster();
        initWithSameCluster(cluster);

        underTest.moveMacsToAnotherMacPoolIfNeeded();

        verifyNoMoreInteractions(moveMacs);
    }

    @Test
    public void testNoChangeWhenMacPoolsDidNotChange() throws Exception {
        Cluster newCluster = createCluster();
        Cluster oldCluster = createCluster();

        newCluster.setMacPoolId(oldCluster.getMacPoolId());

        initOldAndNewCluster(oldCluster, newCluster);
        underTest.moveMacsToAnotherMacPoolIfNeeded();
        verifyNoMoreInteractions(moveMacs);
    }

    @Test
    public void testDoChangeWhenMacPoolsChanged() throws Exception {
        String macToMigrate = "mac";
        Cluster oldCluster = createCluster();
        Cluster newCluster = createCluster();
        initForMovingMacsBetweenClusters(oldCluster, newCluster, macToMigrate);

        underTest.moveMacsToAnotherMacPoolIfNeeded();

        verify(moveMacs).migrateMacsToAnotherMacPool(oldCluster.getMacPoolId(),
                newCluster.getMacPoolId(),
                Collections.singletonList(macToMigrate),
                true,
                underTest.getContext());
    }

    private VM createVm() {
        VM result = new VM();
        result.setId(Guid.newGuid());

        return result;
    }

    private Cluster createCluster() {
        Cluster cluster = new Cluster();

        cluster.setId(Guid.newGuid());
        cluster.setMacPoolId(Guid.newGuid());

        return cluster;
    }

    private void initWithSameCluster(Cluster cluster) {
        initOldAndNewCluster(cluster, cluster);
    }

    private void initOldAndNewCluster(Cluster oldCluster, Cluster newCluster) {
        when(clusterDao.get(oldCluster.getId())).thenReturn(oldCluster);
        when(clusterDao.get(newCluster.getId())).thenReturn(newCluster);

        existingVm.setClusterId(oldCluster.getId());
        existingVm.setStatus(VMStatus.Up);

        parameters.setClusterId(newCluster.getId());

        underTest.setVm(existingVm);
        underTest.init();
    }

    private void initMacToMigrate(String macToMigrate) {
        when(vmNicDao.getAllForVm(existingVm.getId()))
                .thenReturn(Collections.singletonList(macAddressToVmNic(macToMigrate)));
    }

    private VmNic macAddressToVmNic(String macAddress) {
        VmNic result = new VmNic();
        result.setMacAddress(macAddress);
        return result;
    }

    private void initForMovingMacsBetweenClusters(Cluster oldCluster, Cluster newCluster, String macToMigrate) {
        existingVm.setClusterId(oldCluster.getId());

        initOldAndNewCluster(oldCluster, newCluster);
        initMacToMigrate(macToMigrate);
    }
}
