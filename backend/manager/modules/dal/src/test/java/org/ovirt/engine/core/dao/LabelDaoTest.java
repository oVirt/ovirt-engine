package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.network.FirewallType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

import com.google.common.collect.Lists;

public class LabelDaoTest extends BaseDaoTestCase {
    @Inject
    private LabelDao labelDao;

    @Inject
    private VmStaticDao vmDao;

    @Inject
    private VdsStaticDao vdsDao;

    @Inject
    private ClusterDao clusterDao;

    private VdsStatic host;

    @Override
    @Before
    public void setUp() {
        Cluster cluster = new Cluster();
        cluster.setName("test-cluster");
        cluster.setId(Guid.newGuid());
        cluster.setCompatibilityVersion(Version.v3_6);
        cluster.setArchitecture(ArchitectureType.x86);
        cluster.setMacPoolId(FixturesTool.DEFAULT_MAC_POOL_ID);
        cluster.setFirewallType(FirewallType.IPTABLES);

        clusterDao.save(cluster);

        host = new VdsStatic();
        host.setId(Guid.newGuid());
        host.setName("test-host");
        host.setHostName("host-ip");
        host.setClusterId(cluster.getId());

        vdsDao.save(host);
    }

    @Test
    public void testCreateAndGetById() {
        Guid guid = Guid.newGuid();

        Label label = new LabelBuilder()
                .name("test label")
                .id(guid)
                .build();

        labelDao.save(label);
        Label read = labelDao.get(guid);

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

        labelDao.save(label);
        List<Label> readList = labelDao.getAll();

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

        labelDao.save(label);
        Label read = labelDao.get(guid);

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

        labelDao.save(label);
        Label read = labelDao.get(guid);

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

        labelDao.save(label);

        VmStatic vm = createAndSaveVm();

        label = new LabelBuilder()
                .name("test label 2")
                .id(Guid.newGuid())
                .entity(vm)
                .build();

        labelDao.save(label);

        VmStatic vm2 = createAndSaveVm();

        label = new LabelBuilder()
                .name("test label not in result")
                .id(Guid.newGuid())
                .entity(vm2)
                .build();

        labelDao.save(label);

        List<Label> read = labelDao.getAllByEntityIds(Lists.newArrayList(host.getId(), vm.getId(), Guid.newGuid()));

        assertNotNull(read);
        assertEquals(2, read.size());
    }

    @Test
    public void testCreateAndGetByIds() {
        Label label = new LabelBuilder()
                .name("test label")
                .id(Guid.newGuid())
                .entity(host)
                .build();

        labelDao.save(label);

        VmStatic vm = createAndSaveVm();
        VmStatic vm2 = createAndSaveVm();

        Label label2 = new LabelBuilder()
                .name("test label 2")
                .id(Guid.newGuid())
                .entity(vm)
                .build();

        labelDao.save(label2);

        label = new LabelBuilder()
                .name("test label not in result")
                .id(Guid.newGuid())
                .entity(vm2)
                .build();

        labelDao.save(label);

        List<Label> read = labelDao.getAllByIds(Lists.newArrayList(label.getId(), label2.getId()));

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

        labelDao.save(label);

        List<Label> read = labelDao.getAllByEntityIds(Lists.newArrayList(host.getId(), vm.getId()));

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

        labelDao.save(label);
        labelDao.remove(label.getId());
        Label read = labelDao.get(guid);

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

        labelDao.addVmToLabels(vm.getId(), guidsForLabelsToAssign);

        List<Label> assignedLabels = labelDao.getAllByEntityIds(Lists.newArrayList(vm.getId()));

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

        labelDao.addHostToLabels(host.getId(), guidsForLabelsToAssign);

        List<Label> assignedLabels = labelDao.getAllByEntityIds(Lists.newArrayList(host.getId()));

        assertNotNull(assignedLabels);
        assertTrue(labelsToAssign.containsAll(assignedLabels) && assignedLabels.containsAll(labelsToAssign));
    }

    @Test
    public void testUpdateLabelsForVm() {
        VmStatic vm = createAndSaveVm();

        Label label = createAndSaveLabel("test_label");
        labelDao.addVmToLabels(vm.getId(), Lists.newArrayList(label.getId()));

        Label label2 = createAndSaveLabel("test_label_2");

        labelDao.updateLabelsForVm(vm.getId(), Lists.newArrayList(label2.getId()));

        List<Label> assignedLabels = labelDao.getAllByEntityIds(Lists.newArrayList(vm.getId()));

        assertNotNull(assignedLabels);
        assertEquals(Lists.newArrayList(label2), assignedLabels);
    }

    @Test
    public void testUpdateLabelsForHost() {
        Label label = createAndSaveLabel("test_label");
        labelDao.addHostToLabels(host.getId(), Lists.newArrayList(label.getId()));

        Label label2 = createAndSaveLabel("test_label_2");

        labelDao.updateLabelsForHost(host.getId(), Lists.newArrayList(label2.getId()));

        List<Label> assignedLabels = labelDao.getAllByEntityIds(Lists.newArrayList(host.getId()));

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

        labelDao.addVmToLabels(vm.getId(), guidsForLabelsToAssign);

        guidsForLabelsToAssign.remove(label2.getId());
        labelDao.updateLabelsForVm(vm.getId(), guidsForLabelsToAssign);

        List<Label> labelsForVm = labelDao.getAllByEntityIds(Lists.newArrayList(vm.getId()));

        assertTrue(labelsForVm.size() == 1);
        assertEquals(label, labelsForVm.get(0));
    }

    private Label createAndSaveLabel(String labelName) {
        Label label = new LabelBuilder()
                .name(labelName)
                .id(Guid.newGuid())
                .build();

        labelDao.save(label);

        return label;
    }

    private VmStatic createAndSaveVm() {
        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());

        vmDao.save(vm);

        return vm;
    }
}
