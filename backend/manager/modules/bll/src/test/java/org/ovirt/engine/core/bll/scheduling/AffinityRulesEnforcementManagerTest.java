package org.ovirt.engine.core.bll.scheduling;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesEnforcer;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

@RunWith(MockitoJUnitRunner.class)
public class AffinityRulesEnforcementManagerTest {

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            mockConfig(ConfigValues.AffinityRulesEnforcementManagerRegularInterval, 1),
            mockConfig(ConfigValues.AffinityRulesEnforcementManagerInitialDelay, 1)
    );

    @Mock
    private AuditLogDirector auditLogDirector;
    @Mock
    private VdsGroupDao vdsGroupDao;
    @Mock
    private VmDao vmDao;
    @Mock
    private SchedulerUtilQuartzImpl scheduler;
    @Mock
    private BackendInternal backend;

    @Mock
    private AffinityRulesEnforcer rulesEnforcer;
    @Mock
    VM vm1;
    @Mock
    VM vm2;

    @InjectMocks @Spy
    private AffinityRulesEnforcementManager arem;

    private VDSGroup vdsGroup1;

    private VDSGroup vdsGroup2;

    /**
     * Setup a basic scenario with two clusters:
     * - vm1 runs on vdsGroup1
     * - vm2 runs on vdsGroup2.
     * In the default setup  we tell the AffinityRulesEnforcmenetManager, that in each cluster, something needs to be migrated.
     */
    @Before
    public void setup() {
        vdsGroup1 = createVdsGroup();
        vdsGroup2 = createVdsGroup();
        when(vdsGroupDao.getAll()).thenReturn(Arrays.asList(vdsGroup1, vdsGroup2));

        when(rulesEnforcer.chooseNextVmToMigrate(eq(vdsGroup1))).thenReturn(vm1);
        when(rulesEnforcer.chooseNextVmToMigrate(eq(vdsGroup2))).thenReturn(vm2);

        arem.wakeup();
    }

    protected VDSGroup createVdsGroup() {
        Guid id = Guid.newGuid();
        VDSGroup cluster = new VDSGroup();
        cluster.setVdsGroupId(id);
        cluster.setId(id);
        cluster.setName("Default cluster");
        return cluster;
    }

    @Test
    public void shouldMigrateOneVmPerCluster() {
        when(rulesEnforcer.chooseNextVmToMigrate(eq(vdsGroup1))).thenReturn(vm1, mock(VM.class), mock(VM.class));
        arem.refresh();
        verify(arem, times(1)).migrateVM(eq(vm1));
        verify(arem, times(1)).migrateVM(eq(vm2));
        verify(arem, times(2)).migrateVM(any(VM.class));
    }

    @Test
    public void shouldNotMigrateVmOnCluster2MigratingFromCluster() {
        final VM migratingVM = new VM();
        migratingVM.setVdsGroupId(vdsGroup2.getId());
        when(vmDao.getAllByStatus(eq(VMStatus.MigratingFrom))).thenReturn(Arrays.asList(migratingVM));
        arem.refresh();
        verify(arem).migrateVM(vm1);
        verify(arem, times(1)).migrateVM(any(VM.class));
    }

    @Test
    public void shouldNotMigrateVmOnCluster2MigratingToCluster() {
        final VM migratingVM = new VM();
        migratingVM.setVdsGroupId(vdsGroup2.getId());
        when(vmDao.getAllByStatus(eq(VMStatus.MigratingFrom))).thenReturn(Arrays.asList(migratingVM));
        arem.refresh();
        verify(arem).migrateVM(vm1);
        verify(arem, times(1)).migrateVM(any(VM.class));
    }

    @Test
    public void shouldNotMigrateVmOnCluster2NothingToDo() {
        when(rulesEnforcer.chooseNextVmToMigrate(eq(vdsGroup2))).thenReturn(null);
        arem.refresh();
        verify(arem).migrateVM(vm1);
        verify(arem, times(1)).migrateVM(any(VM.class));
    }

    @Test
    public void shouldHaveNotingToMigrate() {
        when(rulesEnforcer.chooseNextVmToMigrate(any(VDSGroup.class))).thenReturn(null);
        verify(arem, never()).migrateVM(any(VM.class));
    }

    @Test
    public void shouldScheduleRegularInterval() {
        verify(scheduler).scheduleAFixedDelayJob(anyObject(),
                eq("refresh"),
                eq(new Class[] {}),
                eq(new Object[] {}),
                eq(1L),
                anyLong(),
                eq(TimeUnit.MINUTES));
    }
}
