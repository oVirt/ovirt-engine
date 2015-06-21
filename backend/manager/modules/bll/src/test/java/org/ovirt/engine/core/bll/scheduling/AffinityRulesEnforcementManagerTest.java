package org.ovirt.engine.core.bll.scheduling;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.InjectorRule;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesEnforcementPerCluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;


import javax.enterprise.inject.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

@RunWith(MockitoJUnitRunner.class)
public class AffinityRulesEnforcementManagerTest {
    private AffinityRulesEnforcementManager arem;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule(
            mockConfig(ConfigValues.AffinityRulesEnforcementManagerRegularInterval, 1),
            mockConfig(ConfigValues.AffinityRulesEnforcementManagerInitialDelay, 1),
            mockConfig(ConfigValues.AffinityRulesEnforcementManagerMaximumMigrationTries, 1),
            mockConfig(ConfigValues.AffinityRulesEnforcementManagerStandbyInterval, 1),
            mockConfig(ConfigValues.VdsLoadBalancingIntervalInMinutes, 1),
            mockConfig(ConfigValues.VdsHaReservationIntervalInMinutes, 1),
            mockConfig(ConfigValues.EnableVdsLoadBalancing, true)
    );

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    private AffinityRulesEnforcementPerCluster perCluster;

    @Mock
    private VdsGroupDAO _vdsGroupDao; //Clusters

    @Mock
    private AffinityGroupDao _affinityGroupDao;

    private VDSGroup cluster;

    @Mock
    private VdsDAO _vdsDao;

    @Mock
    private VmDAO _vmDao;

    @Mock
    private AuditLogDirector _auditLogDirector;

    @Mock
    private SchedulerUtilQuartzImpl scheduler;

    @Mock
    private Instance<AffinityRulesEnforcementPerCluster> _perClusterProvider;

    private class TestingAffinityRulesEnforcementPerCluster extends AffinityRulesEnforcementPerCluster {
        private TestingAffinityRulesEnforcementPerCluster() {
            this.affinityGroupDao = _affinityGroupDao;
            this.vmDao = _vmDao;
            this.vdsDao = _vdsDao;
            this.vdsGroupDao = _vdsGroupDao;
        }
    }

    @Before
    public void setup() {
        initVdsGroup();
        initMocks();

        arem = new AffinityRulesEnforcementManager() {
            @Override
            public void wakeup() {
                this.auditLogDirector = _auditLogDirector;
                this.vdsDao = _vdsDao;
                this.vdsGroupDao = _vdsGroupDao;

                doReturn(new TestingAffinityRulesEnforcementPerCluster()).when(_perClusterProvider).get();

                this.perClusterProvider = _perClusterProvider;

                super.wakeup();

                addInjectionsToPerClusterObjects();
                }

                @Override
            public void refresh() {
                addInjectionsToPerClusterObjects();
                super.refresh();
            }

            private void addInjectionsToPerClusterObjects() {
                //Adding affinity group dao to all perCluster objects in the maps.
                for(Entry<VDSGroup, AffinityRulesEnforcementPerCluster> entry: arem.perClusterMap.entrySet()) {
                    AffinityRulesEnforcementPerCluster perCluster = perClusterProvider.get();
                    perCluster.setClusterId(entry.getKey().getId());
                    entry.setValue(perCluster);
                }
            }

            @Override
            protected List<VDSGroup> getClusters() {
                List<VDSGroup> vdsGroups = new ArrayList<>();
                vdsGroups.add(cluster);
                return vdsGroups;
            }

        };
        arem.wakeup();
        arem.refresh();

        for(AffinityRulesEnforcementPerCluster perCluster : arem.perClusterMap.values()) {
            perCluster.wakeup();
        }
    }

    protected void initVdsGroup() {
        //Initiating cluster
        Guid id = Guid.newGuid();
        cluster = new VDSGroup();
        cluster.setVdsGroupId(id);
        cluster.setId(id);
        cluster.setName("Default cluster");
    }

    protected void initMocks() {
        injectorRule.bind(SchedulerUtilQuartzImpl.class, scheduler);
        when(scheduler.scheduleAFixedDelayJob(any(),
                anyString(),
                any(Class[].class),
                any(Object[].class),
                anyInt(),
                anyInt(),
                any(TimeUnit.class)
        )).thenReturn("jobId");
    }

    @Test
    public void simplePositiveEnforcementUAGTest() {
        VDSGroup vdsGroup = arem.perClusterMap.keySet().iterator().next();

        //Add 2 hosts
        VDS vdsId1 = _vdsDao.get(addHost(vdsGroup.getId()));
        VDS vdsId2 = _vdsDao.get(addHost(vdsGroup.getId()));

        //Creating Affinity Group list with one positive affinity groups.
        List<Guid> agList = new ArrayList<>();

        List<Guid> vmList = new ArrayList<>();
        vmList.add(addNewVm(vdsId1.getId(), true));
        vmList.add(addNewVm(vdsId2.getId(), true));
        agList.add(addAffinityGroup(vmList, vdsGroup.getId(), true));

        arem.refresh();
    }

    @Test
    public void positiveEnforcementUAGTest() {
        VDSGroup vdsGroup = arem.perClusterMap.keySet().iterator().next();
        AffinityRulesEnforcementPerCluster perCluster = arem.perClusterMap.get(vdsGroup);

        //Creating Affinity Group list with two positive affinity groups.
        List<Guid> agList = new ArrayList<>();

        List<Guid> vmList = new ArrayList<>();
        vmList.add(addNewVm(vdsGroup.getId(), true));
        agList.add(addAffinityGroup(vmList, vdsGroup.getId(), true));

        vmList = new ArrayList<>();
        vmList.add(addNewVm(vdsGroup.getId(), true));
        agList.add(addAffinityGroup(vmList, vdsGroup.getId(), true));

        //Adding new Vm for both affinity groups
        Guid vmId = addNewVm(vdsGroup.getId(), true);
        for(Guid id : agList) {
            AffinityGroup ag = _affinityGroupDao.get(id);
            List<Guid> entities = ag.getEntityIds();
            entities.add(vmId);
            ag.setEntityIds(entities);
        }

        assertNull(perCluster.chooseNextVmToMigrate());
    }

    private Guid addNewVm(Guid vdsToRunOn, Boolean isRunning) {
        Guid guid = Guid.newGuid();

        VMStatus isResponding = VMStatus.Up; //All vms status is up

        VM vm = mock(VM.class);
        when(_vmDao.get(guid)).thenReturn(vm);
        doReturn(vdsToRunOn).when(vm).getRunOnVds();
        doReturn(isRunning).when(vm).isRunning();
        doReturn(isResponding).when(vm).getStatus();


        _vmDao.saveIsInitialized(guid, true);

        String outputStr = String.format("New VM[%s] sits on host[%s]", guid, vdsToRunOn);
        System.out.println(outputStr);
        return guid;
    }

    private Guid addHost(Guid vdsGroupId) {
        Guid id = Guid.newGuid();

        VDS vds = mock(VDS.class);
        doReturn(id).when(vds).getId();
        doReturn(vdsGroupId).when(vds).getVdsGroupId();

        when(_vdsDao.get(id)).thenReturn(vds);

        List<VDS> vdsList = _vdsDao.getAllForVdsGroup(vdsGroupId);
        vdsList.add(vds);

        when(_vdsDao.getAllForVdsGroup(vdsGroupId)).thenReturn(vdsList);

        String outputStr = String.format("New Host[%s] sits on cluster[%s]", id, vdsGroupId);
        System.out.println(outputStr);

        return id;
    }

    private Guid addAffinityGroup(List<Guid> vmList, Guid vdsGroupId, Boolean isPositive) {
        Guid id = Guid.newGuid();
        AffinityGroup ag = mock(AffinityGroup.class);
        doReturn(id).when(ag).getId();
        doReturn(isPositive).when(ag).isPositive();
        doReturn(vmList).when(ag).getEntityIds();
        doReturn(vdsGroupId).when(ag).getClusterId();

        when(_affinityGroupDao.get(id)).thenReturn(ag);

        //Adding when() for getAllAffinityGroupsByClusterId() to return affinity groups list.
        List<AffinityGroup> agList = _affinityGroupDao.getAllAffinityGroupsByClusterId(vdsGroupId);
        agList.add(ag);

        when(_affinityGroupDao.getAllAffinityGroupsByClusterId(vdsGroupId)).thenReturn(agList);

        //Adding when() for getAll()
        agList = _affinityGroupDao.getAll();
        agList.add(ag);

        when(_affinityGroupDao.getAll()).thenReturn(agList);

        String outputStr = String.format("New AffinityGroup[%s] sits on cluster[%s]\nwith Vms[%s]", id, vdsGroupId, vmList);
        System.out.println(outputStr);

        return id;
    }
}
