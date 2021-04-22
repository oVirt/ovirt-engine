package org.ovirt.engine.core.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.LogMaxMemoryUsedThresholdType;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.network.FirewallType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

import com.google.common.collect.Lists;

public class LabelDaoTest extends BaseDaoTestCase<LabelDao> {
    @Inject
    private VmStaticDao vmDao;

    @Inject
    private VdsStaticDao vdsDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private CpuProfileDao cpuProfileDao;

    private Guid cluster1 = FixturesTool.CLUSTER_RHEL6_NFS;
    private Guid cluster2 = FixturesTool.CLUSTER_RHEL6_NFS_2;

    private VdsStatic host;

    @BeforeEach
    @Override
    public void setUp() {
        host = createAndSaveHost("test-host", cluster1);
    }

    @Test
    public void testCreateAndGetById() {
        Guid guid = Guid.newGuid();

        Label label = new LabelBuilder()
                .name("test label")
                .id(guid)
                .build();

        dao.save(label);
        Label read = dao.get(guid);

        assertNotNull(read);
        assertEquals(guid, read.getId());
        assertEquals(label.getName(), read.getName());
    }

    @Test
    public void testCreateAndGetAll() {
        Guid guid = Guid.newGuid();

        Label label = new LabelBuilder()
                .name("test label")
                .id(guid)
                .build();

        dao.save(label);
        List<Label> readList = dao.getAll();

        assertNotNull(readList);

        Label read = readList.get(0);

        assertNotNull(read);
        assertEquals(guid, read.getId());
        assertEquals(label.getName(), read.getName());
    }

    @Test
    public void testCreateAndGetWithVM() {
        Guid guid = Guid.newGuid();

        VmStatic vm = createAndSaveVm();

        Label label = new LabelBuilder()
                .name("test label")
                .id(guid)
                .vm(vm.getId())
                .build();

        dao.save(label);
        Label read = dao.get(guid);

        assertNotNull(read);
        assertEquals(guid, read.getId());
        assertEquals(label.getName(), read.getName());
        assertNotNull(read.getVms());
        assertEquals(1, read.getVms().size());
        assertEquals(0, read.getHosts().size());
        assertEquals(vm.getId(), read.getVms().iterator().next());
    }

    @Test
    public void testCreateAndGetWithHost() {
        Guid guid = Guid.newGuid();

        Label label = new LabelBuilder()
                .name("test label")
                .id(guid)
                .entity(host)
                .build();

        dao.save(label);
        Label read = dao.get(guid);

        assertNotNull(read);
        assertEquals(guid, read.getId());
        assertEquals(label.getName(), read.getName());
        assertNotNull(read.getVms());
        assertEquals(0, read.getVms().size());
        assertEquals(1, read.getHosts().size());
        assertEquals(host.getId(), read.getHosts().iterator().next());
    }

    @Test
    public void testCreateAndGetByReferencedId() {
        Label label = new LabelBuilder()
                .name("test label")
                .id(Guid.newGuid())
                .entity(host)
                .build();

        dao.save(label);

        VmStatic vm = createAndSaveVm();

        label = new LabelBuilder()
                .name("test label 2")
                .id(Guid.newGuid())
                .entity(vm)
                .build();

        dao.save(label);

        VmStatic vm2 = createAndSaveVm();

        label = new LabelBuilder()
                .name("test label not in result")
                .id(Guid.newGuid())
                .entity(vm2)
                .build();

        dao.save(label);

        List<Label> read = dao.getAllByEntityIds(Lists.newArrayList(host.getId(), vm.getId(), Guid.newGuid()));

        assertNotNull(read);
        assertEquals(2, read.size());
    }

    @Test
    public void testCreateAndGetByClusterId() {
        Label label1 = new LabelBuilder()
                .name("test label")
                .entity(host)
                .build();

        VdsStatic host2 = createAndSaveHost("host2", cluster2);
        Label label2 = new LabelBuilder()
                .name("test label 2")
                .entity(host2)
                .build();

        VmStatic vm1 = createAndSaveVm(cluster1);
        Label label3 = new LabelBuilder()
                .name("test label 3")
                .entity(vm1)
                .build();

        VmStatic vm2 = createAndSaveVm(cluster2);
        Label label4 = new LabelBuilder()
                .name("test label 4")
                .entity(vm2)
                .build();

        dao.save(label1);
        dao.save(label2);
        dao.save(label3);
        dao.save(label4);

        List<Label> read = dao.getAllByClusterId(cluster1);
        assertThat(read).containsOnly(label1, label3);
    }

    @Test
    public void testCreateAndGetByIds() {
        Label label = new LabelBuilder()
                .name("test label")
                .id(Guid.newGuid())
                .entity(host)
                .build();

        dao.save(label);

        VmStatic vm = createAndSaveVm();
        VmStatic vm2 = createAndSaveVm();

        Label label2 = new LabelBuilder()
                .name("test label 2")
                .id(Guid.newGuid())
                .entity(vm)
                .build();

        dao.save(label2);

        label = new LabelBuilder()
                .name("test label not in result")
                .id(Guid.newGuid())
                .entity(vm2)
                .build();

        dao.save(label);

        List<Label> read = dao.getAllByIds(Lists.newArrayList(label.getId(), label2.getId()));

        assertNotNull(read);
        assertEquals(2, read.size());
    }

    @Test
    public void testCreateAndGetLabelWithTwoItems() {
        VmStatic vm = createAndSaveVm();

        Label label = new LabelBuilder()
                .name("test label")
                .id(Guid.newGuid())
                .entities(host, vm)
                .build();

        dao.save(label);

        List<Label> read = dao.getAllByEntityIds(Lists.newArrayList(host.getId(), vm.getId()));

        assertNotNull(read);
        assertEquals(1, read.size());
    }

    @Test
    public void testCreateAndDeleteById() {
        Guid guid = Guid.newGuid();

        Label label = new LabelBuilder()
                .name("test label")
                .id(guid)
                .host(host.getId())
                .build();

        dao.save(label);
        dao.remove(label.getId());
        Label read = dao.get(guid);

        assertNull(read);
    }

    @Test
    public void testAddVmToLabels() {
        Label label = createAndSaveLabel("test_label");
        Label label2 = createAndSaveLabel("test_label_2");
        VmStatic vm = createAndSaveVm();

        List<Label> labelsToAssign = Lists.newArrayList(label, label2);
        List<Guid> guidsForLabelsToAssign = labelsToAssign.stream()
                .map(Label::getId)
                .collect(Collectors.toList());

        dao.addVmToLabels(vm.getId(), guidsForLabelsToAssign);

        List<Label> assignedLabels = dao.getAllByEntityIds(Lists.newArrayList(vm.getId()));

        assertNotNull(assignedLabels);
        assertTrue(labelsToAssign.containsAll(assignedLabels) && assignedLabels.containsAll(labelsToAssign));
    }

    @Test
    public void testAddHostToLabels() {
        Label label = createAndSaveLabel("test_label");
        Label label2 = createAndSaveLabel("test_label_2");

        List<Label> labelsToAssign = Lists.newArrayList(label, label2);
        List<Guid> guidsForLabelsToAssign = labelsToAssign.stream()
                .map(Label::getId)
                .collect(Collectors.toList());

        dao.addHostToLabels(host.getId(), guidsForLabelsToAssign);

        List<Label> assignedLabels = dao.getAllByEntityIds(Lists.newArrayList(host.getId()));

        assertNotNull(assignedLabels);
        assertTrue(labelsToAssign.containsAll(assignedLabels) && assignedLabels.containsAll(labelsToAssign));
    }

    @Test
    public void testUpdateLabelsForVm() {
        VmStatic vm = createAndSaveVm();

        Label label = createAndSaveLabel("test_label");
        dao.addVmToLabels(vm.getId(), Lists.newArrayList(label.getId()));

        Label label2 = createAndSaveLabel("test_label_2");

        dao.updateLabelsForVm(vm.getId(), Lists.newArrayList(label2.getId()));

        List<Label> assignedLabels = dao.getAllByEntityIds(Lists.newArrayList(vm.getId()));

        assertNotNull(assignedLabels);
        assertEquals(Lists.newArrayList(label2), assignedLabels);
    }

    @Test
    public void testUpdateLabelsForHost() {
        Label label = createAndSaveLabel("test_label");
        dao.addHostToLabels(host.getId(), Lists.newArrayList(label.getId()));

        Label label2 = createAndSaveLabel("test_label_2");

        dao.updateLabelsForHost(host.getId(), Lists.newArrayList(label2.getId()));

        List<Label> assignedLabels = dao.getAllByEntityIds(Lists.newArrayList(host.getId()));

        assertNotNull(assignedLabels);
        assertEquals(Lists.newArrayList(label2), assignedLabels);
    }

    @Test
    public void testRemoveLabelFromVm() {
        Label label = createAndSaveLabel("test_label");
        Label label2 = createAndSaveLabel("test_label_2");
        VmStatic vm = createAndSaveVm();

        List<Label> labelsToAssign = Lists.newArrayList(label, label2);
        List<Guid> guidsForLabelsToAssign = labelsToAssign.stream()
                .map(Label::getId)
                .collect(Collectors.toList());

        dao.addVmToLabels(vm.getId(), guidsForLabelsToAssign);

        guidsForLabelsToAssign.remove(label2.getId());
        dao.updateLabelsForVm(vm.getId(), guidsForLabelsToAssign);

        List<Label> labelsForVm = dao.getAllByEntityIds(Lists.newArrayList(vm.getId()));

        assertEquals(1, labelsForVm.size());
        assertEquals(label, labelsForVm.get(0));
    }

    @Test
    public void testCreateAndGetWithImplicitAffinityGroup() {
        Guid guid = Guid.newGuid();
        Label label = new LabelBuilder()
                .name("test_label")
                .id(guid)
                .implicitAffinityGroup(true)
                .build();

        dao.save(label);

        Guid guid2 = Guid.newGuid();
        Label label2 = new LabelBuilder()
                .name("test_label_2")
                .id(guid2)
                .implicitAffinityGroup(false)
                .build();

        dao.save(label2);

        Label labelFromDb = dao.get(guid);
        Label labelFromDb2 = dao.get(guid2);

        assertEquals(label.isImplicitAffinityGroup(), labelFromDb.isImplicitAffinityGroup());
        assertEquals(label2.isImplicitAffinityGroup(), labelFromDb2.isImplicitAffinityGroup());
    }

    private Label createAndSaveLabel(String labelName) {
        Label label = new LabelBuilder()
                .name(labelName)
                .id(Guid.newGuid())
                .build();

        dao.save(label);

        return label;
    }

    private VmStatic createAndSaveVm() {
        return createAndSaveVm(cluster1);
    }

    private VmStatic createAndSaveVm(Guid clusterId) {
        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vm.setClusterId(clusterId);
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        vm.setCpuProfileId(cpuProfileDao.getAllForCluster(clusterId).get(0).getId());

        vmDao.save(vm);

        return vm;
    }

    private VdsStatic createAndSaveHost(String name, Guid clusterId) {
        VdsStatic host = new VdsStatic();
        host.setId(Guid.newGuid());
        host.setName(name);
        host.setHostName(name);
        host.setClusterId(clusterId);
        vdsDao.save(host);

        return host;
    }

    private Cluster createAndSaveCluster(String name) {
        Cluster cluster = new Cluster();
        cluster.setName(name);
        cluster.setId(Guid.newGuid());
        cluster.setCompatibilityVersion(Version.v4_3);
        cluster.setArchitecture(ArchitectureType.x86);
        cluster.setMacPoolId(FixturesTool.DEFAULT_MAC_POOL_ID);
        cluster.setFirewallType(FirewallType.IPTABLES);
        cluster.setLogMaxMemoryUsedThreshold(95);
        cluster.setLogMaxMemoryUsedThresholdType(LogMaxMemoryUsedThresholdType.PERCENTAGE);

        clusterDao.save(cluster);
        return cluster;
    }
}
