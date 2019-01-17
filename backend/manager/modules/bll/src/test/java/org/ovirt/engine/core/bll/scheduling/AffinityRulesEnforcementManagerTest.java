package org.ovirt.engine.core.bll.scheduling;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesEnforcer;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class AffinityRulesEnforcementManagerTest {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.AffinityRulesEnforcementManagerInitialDelay, 1L),
                MockConfigDescriptor.of(ConfigValues.AffinityRulesEnforcementManagerRegularInterval, 1L)
        );
    }

    @Mock
    private AuditLogDirector auditLogDirector;
    @Mock
    private ClusterDao clusterDao;
    @Mock
    private BackendInternal backend;

    @Mock
    private AffinityRulesEnforcer rulesEnforcer;
    @Mock
    VM vm1;
    @Mock
    VM vm2;

    @Mock
    private ManagedScheduledExecutorService executor;

    @InjectMocks
    @Spy
    private AffinityRulesEnforcementManager arem;

    private Cluster cluster1;

    private Cluster cluster2;

    /**
     * Setup a basic scenario with two clusters:
     * - vm1 runs on cluster1
     * - vm2 runs on cluster2.
     * In the default setup  we tell the AffinityRulesEnforcmenetManager, that in each cluster, something needs to be migrated.
     */
    @BeforeEach
    public void setup() {
        cluster1 = createCluster();
        cluster2 = createCluster();
        when(clusterDao.getWithoutMigratingVms()).thenReturn(Arrays.asList(cluster1, cluster2));

        when(rulesEnforcer.chooseVmsToMigrate(eq(cluster1))).thenReturn(Collections.singletonList(vm1).iterator());
        when(rulesEnforcer.chooseVmsToMigrate(eq(cluster2))).thenReturn(Collections.singletonList(vm2).iterator());

        ActionReturnValue returnValue = new ActionReturnValue();
        returnValue.setSucceeded(true);

        when(backend.runInternalAction(any(), any(), any())).thenReturn(returnValue);

        arem.wakeup();
    }

    protected Cluster createCluster() {
        Guid id = Guid.newGuid();
        Cluster cluster = new Cluster();
        cluster.setClusterId(id);
        cluster.setId(id);
        cluster.setName("Default cluster");
        return cluster;
    }

    @Test
    public void shouldMigrateOneVmPerCluster() {
        when(rulesEnforcer.chooseVmsToMigrate(eq(cluster1)))
                .thenReturn(Arrays.asList(vm1, mock(VM.class), mock(VM.class)).iterator());

        arem.refresh();
        verify(arem, times(1)).migrateVM(eq(vm1));
        verify(arem, times(1)).migrateVM(eq(vm2));
        verify(arem, times(2)).migrateVM(any());
    }

    @Test
    public void shouldNotMigrateVmOnClusterTwoWhileMigrating() {
        final VM migratingVM = new VM();
        migratingVM.setClusterId(cluster2.getId());
        when(clusterDao.getWithoutMigratingVms()).thenReturn(Collections.singletonList(cluster1));
        arem.refresh();
        verify(arem).migrateVM(vm1);
        verify(arem, times(1)).migrateVM(any());
    }

    @Test
    public void shouldNotMigrateVmOnClusterTwoWhileInUpgradeMode() {
        cluster2.setClusterPolicyId(ClusterPolicy.UPGRADE_POLICY_GUID);
        arem.refresh();
        verify(arem).migrateVM(vm1);
        verify(arem, times(1)).migrateVM(any());
        verify(arem, times(0)).migrateVM(eq(vm2));
    }

    @Test
    public void shouldNotMigrateVmOnClusterTwoWhenEnforced() {
        when(rulesEnforcer.chooseVmsToMigrate(eq(cluster2))).thenReturn(null);
        arem.refresh();
        verify(arem).migrateVM(vm1);
        verify(arem, times(1)).migrateVM(any());
    }

    @Test
    public void shouldHaveNotingToMigrate() {
        verify(arem, never()).migrateVM(any());
    }

}
