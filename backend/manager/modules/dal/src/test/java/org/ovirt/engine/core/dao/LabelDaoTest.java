package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VdsProtocol;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
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

    @Before
    public void setUp() {
        Cluster cluster = new Cluster();
        cluster.setName("test-cluster");
        cluster.setId(Guid.newGuid());
        cluster.setCompatibilityVersion(Version.v3_6);
        cluster.setArchitecture(ArchitectureType.x86);

        clusterDao.save(cluster);

        host = new VdsStatic();
        host.setId(Guid.newGuid());
        host.setName("test-host");
        host.setHostName("host-ip");
        host.setClusterId(cluster.getId());
        host.setProtocol(VdsProtocol.STOMP);

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

        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vmDao.save(vm);

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

        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vmDao.save(vm);

        label = new LabelBuilder()
                .name("test label 2")
                .id(Guid.newGuid())
                .entity(vm)
                .build();

        labelDao.save(label);

        VmStatic vm2 = new VmStatic();
        vm2.setId(Guid.newGuid());
        vmDao.save(vm2);

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

        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vmDao.save(vm);

        VmStatic vm2 = new VmStatic();
        vm2.setId(Guid.newGuid());
        vmDao.save(vm2);

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
        VmStatic vm = new VmStatic();
        vm.setId(Guid.newGuid());
        vmDao.save(vm);

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
}
