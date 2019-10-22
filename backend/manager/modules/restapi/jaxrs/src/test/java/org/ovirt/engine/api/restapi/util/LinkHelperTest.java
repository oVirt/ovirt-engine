/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Event;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Statistic;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;

public class LinkHelperTest {

    private static final String VM_ID = "awesome";
    private static final String CLUSTER_ID = "alarming";
    private static final String TEMPLATE_ID = "astonishing";
    private static final String VM_POOL_ID = "beautiful";
    private static final String STORAGE_DOMAIN_ID = "breathtaking";
    private static final String HOST_ID = "magnificent";
    private static final String DATA_CENTER_ID = "majestic";
    private static final String NETWORK_ID = "stupendous";
    private static final String TAG_ID = "outstanding";
    private static final String FILE_ID = "faroutdude";
    private static final String CDROM_ID = "wonderful";
    private static final String NIC_ID = "super";
    private static final String STORAGE_ID = "sensational";
    private static final String USER_ID = "doublerainbowalltheway";
    private static final String GROUP_ID = "bankruptnation";
    private static final String EVENT_ID = "eventtest";
    private static final String STATISTIC_ID = "bleedindeadly";

    private static final String URI_ROOT = "http://localhost:8700";
    private static final String BASE_PATH = "/ovirt-engine/api";

    private static final String VM_HREF = BASE_PATH + "/vms/" + VM_ID;
    private static final String CLUSTER_HREF = BASE_PATH + "/clusters/" + CLUSTER_ID;
    private static final String TEMPLATE_HREF = BASE_PATH + "/templates/" + TEMPLATE_ID;
    private static final String VM_POOL_HREF = BASE_PATH + "/vmpools/" + VM_POOL_ID;
    private static final String STORAGE_DOMAIN_HREF = BASE_PATH + "/storagedomains/" + STORAGE_DOMAIN_ID;
    private static final String ATTACHED_STORAGE_DOMAIN_HREF = BASE_PATH + "/datacenters/" + DATA_CENTER_ID + "/storagedomains/" + STORAGE_DOMAIN_ID;
    private static final String STORAGE_DOMAIN_VM_HREF = STORAGE_DOMAIN_HREF + "/vms/" + VM_ID;
    private static final String STORAGE_DOMAIN_TEMPLATE_HREF = STORAGE_DOMAIN_HREF + "/templates/" + TEMPLATE_ID;
    private static final String HOST_HREF = BASE_PATH + "/hosts/" + HOST_ID;
    private static final String DATA_CENTER_HREF = BASE_PATH + "/datacenters/" + DATA_CENTER_ID;
    private static final String NETWORK_HREF = BASE_PATH + "/networks/" + NETWORK_ID;
    private static final String TAG_HREF = BASE_PATH + "/tags/" + TAG_ID;
    private static final String VM_TAG_HREF = BASE_PATH + "/vms/" + VM_ID + "/tags/" + TAG_ID;
    private static final String HOST_TAG_HREF = BASE_PATH + "/hosts/" + HOST_ID + "/tags/" + TAG_ID;
    private static final String TEMPLATE_TAG_HREF = BASE_PATH + "/templates/" + TEMPLATE_ID + "/tags/" + TAG_ID;
    private static final String USER_TAG_HREF = BASE_PATH + "/users/" + USER_ID + "/tags/" + TAG_ID;
    private static final String CLUSTER_NETWORK_HREF = BASE_PATH + "/clusters/" + CLUSTER_ID + "/networks/" + NETWORK_ID;
    private static final String FILE_HREF = BASE_PATH + "/storagedomains/" + STORAGE_DOMAIN_ID + "/files/" + FILE_ID;
    private static final String CDROM_HREF = VM_HREF + "/cdroms/" + CDROM_ID;
    private static final String NIC_HREF = VM_HREF + "/nics/" + NIC_ID;
    private static final String STORAGE_HREF = HOST_HREF + "/storage/" + STORAGE_ID;
    private static final String GROUP_HREF = BASE_PATH + "/groups/" + GROUP_ID;
    private static final String EVENT_HREF = BASE_PATH + "/events/" + EVENT_ID;
    private static final String STATISTIC_HREF = VM_HREF + "/statistics/" + STATISTIC_ID;

    @BeforeEach
    public void setUp() {
        Current current = new Current();
        current.setRoot(URI_ROOT);
        current.setPrefix(BASE_PATH);
        current.setPath("");
        CurrentManager.put(current);
    }

    @AfterEach
    public void tearDown() {
        CurrentManager.remove();
    }

    @Test
    public void testEventLinks() {
        Event event = new Event();
        event.setId(EVENT_ID);

        LinkHelper.addLinks(event);

        assertEquals(EVENT_HREF, event.getHref());
    }

    @Test
    public void testVmLinks() {
        doTestVmLinks(false);
    }

    @Test
    public void testVmLinksSuggestedParent() {
        doTestVmLinks(true);
    }

    private void doTestVmLinks(boolean suggestParent) {
        Vm vm = new Vm();
        vm.setId(VM_ID);
        vm.setCluster(new Cluster());
        vm.getCluster().setId(CLUSTER_ID);
        vm.setTemplate(new Template());
        vm.getTemplate().setId(TEMPLATE_ID);
        vm.setVmPool(new VmPool());
        vm.getVmPool().setId(VM_POOL_ID);

        if (suggestParent) {
            LinkHelper.addLinks(vm, Vm.class);
        } else {
            LinkHelper.addLinks(vm);
        }

        assertEquals(VM_HREF, vm.getHref());
        assertEquals(CLUSTER_HREF, vm.getCluster().getHref());
        assertEquals(TEMPLATE_HREF, vm.getTemplate().getHref());
        assertEquals(VM_POOL_HREF, vm.getVmPool().getHref());
    }

    @Test
    public void testClusterLinks() {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        cluster.setDataCenter(new DataCenter());
        cluster.getDataCenter().setId(DATA_CENTER_ID);

        LinkHelper.addLinks(cluster, LinkHelper.NO_PARENT);

        assertEquals(CLUSTER_HREF, cluster.getHref());
        assertEquals(DATA_CENTER_HREF, cluster.getDataCenter().getHref());
    }

    @Test
    public void testHostLinks() {
        Host host = new Host();
        host.setId(HOST_ID);

        LinkHelper.addLinks(host);

        assertEquals(HOST_HREF, host.getHref());
    }

    @Test
    public void testStorageDomainLinks() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(STORAGE_DOMAIN_ID);

        storageDomain.setStorage(new HostStorage());
        storageDomain.getStorage().setPath("foo");

        LinkHelper.addLinks(storageDomain);

        assertEquals(STORAGE_DOMAIN_HREF, storageDomain.getHref());
        assertNull(storageDomain.getStorage().getHref());
    }

    @Test
    public void testAttachedStorageDomainLinks() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(STORAGE_DOMAIN_ID);

        storageDomain.setDataCenter(new DataCenter());
        storageDomain.getDataCenter().setId(DATA_CENTER_ID);

        LinkHelper.addLinks(storageDomain);

        assertEquals(ATTACHED_STORAGE_DOMAIN_HREF, storageDomain.getHref());
    }

    @Test
    public void testStorageDomainVmLinks() {
        Vm vm = new Vm();
        vm.setId(VM_ID);

        vm.setStorageDomain(new StorageDomain());
        vm.getStorageDomain().setId(STORAGE_DOMAIN_ID);

        vm = LinkHelper.addLinks(vm);

        assertEquals(STORAGE_DOMAIN_VM_HREF, vm.getHref());
        assertEquals(STORAGE_DOMAIN_HREF, vm.getStorageDomain().getHref());
    }

    @Test
    public void testStorageDomainTemplateLinks() {
        Template template = new Template();
        template.setId(TEMPLATE_ID);

        template.setStorageDomain(new StorageDomain());
        template.getStorageDomain().setId(STORAGE_DOMAIN_ID);

        template = LinkHelper.addLinks(template);

        assertEquals(STORAGE_DOMAIN_TEMPLATE_HREF, template.getHref());
        assertEquals(STORAGE_DOMAIN_HREF, template.getStorageDomain().getHref());
    }

    @Test
    public void testDataCenterLinks() {
        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(DATA_CENTER_ID);

        LinkHelper.addLinks(dataCenter);

        assertEquals(DATA_CENTER_HREF, dataCenter.getHref());
    }

    @Test
    public void testNetworkLinks() {
        Network network = new Network();
        network.setId(NETWORK_ID);

        LinkHelper.addLinks(network);

        assertEquals(NETWORK_HREF, network.getHref());
    }

    @Test
    public void testClusterNetworkLinks() {
        Network network = new Network();
        network.setId(NETWORK_ID);
        network.setCluster(new Cluster());
        network.getCluster().setId(CLUSTER_ID);

        LinkHelper.addLinks(network);

        assertEquals(CLUSTER_NETWORK_HREF, network.getHref());
    }

    @Test
    public void testTagLinks() {
        Tag tag = new Tag();
        tag.setId(TAG_ID);

        LinkHelper.addLinks(tag);

        assertEquals(TAG_HREF, tag.getHref());
    }

    @Test
    public void testVmTagLinks() {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setVm(new Vm());
        tag.getVm().setId(VM_ID);

        LinkHelper.addLinks(tag);

        assertEquals(VM_TAG_HREF, tag.getHref());
    }

    @Test
    public void testHostTagLinks() {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setHost(new Host());
        tag.getHost().setId(HOST_ID);

        LinkHelper.addLinks(tag);

        assertEquals(HOST_TAG_HREF, tag.getHref());
    }

    @Test
    public void testTemplateTagLinks() {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setTemplate(new Template());
        tag.getTemplate().setId(TEMPLATE_ID);

        LinkHelper.addLinks(tag);

        assertEquals(TEMPLATE_TAG_HREF, tag.getHref());
    }

    @Test
    public void testUserTagLinks() {
        Tag tag = new Tag();
        tag.setId(TAG_ID);
        tag.setUser(new User());
        tag.getUser().setId(USER_ID);

        LinkHelper.addLinks(tag);

        assertEquals(USER_TAG_HREF, tag.getHref());
    }

    @Test
    public void testFileLinks() {
        File file = new File();
        file.setId(FILE_ID);

        file.setStorageDomain(new StorageDomain());
        file.getStorageDomain().setId(STORAGE_DOMAIN_ID);

        LinkHelper.addLinks(file);

        assertEquals(FILE_HREF, file.getHref());
    }

    @Test
    public void testCdRomLinks() {
        Cdrom cdrom = new Cdrom();
        cdrom.setId(CDROM_ID);

        cdrom.setVm(new Vm());
        cdrom.getVm().setId(VM_ID);

        LinkHelper.addLinks(cdrom);

        assertEquals(CDROM_HREF, cdrom.getHref());
    }

    @Test
    public void testNicLinks() {
        Nic nic = new Nic();
        nic.setId(NIC_ID);

        nic.setVm(new Vm());
        nic.getVm().setId(VM_ID);

        LinkHelper.addLinks(nic);

        assertEquals(NIC_HREF, nic.getHref());
    }

    @Test
    public void testStorageLinks() {
        HostStorage storage = new HostStorage();
        storage.setId(STORAGE_ID);

        storage.setHost(new Host());
        storage.getHost().setId(HOST_ID);

        LinkHelper.addLinks(storage);

        assertEquals(STORAGE_HREF, storage.getHref());
        assertEquals(HOST_HREF, storage.getHost().getHref());
    }

    @Test
    public void testGroupLinks() {
        Group group = new Group();
        group.setId(GROUP_ID);

        LinkHelper.addLinks(group);

        assertEquals(GROUP_HREF, group.getHref());
    }

    @Test
    public void testStatisticLinks() {
        Statistic statistic = new Statistic();
        statistic.setId(STATISTIC_ID);

        statistic.setVm(new Vm());
        statistic.getVm().setId(VM_ID);

        LinkHelper.addLinks(statistic);

        assertEquals(STATISTIC_HREF, statistic.getHref());
    }
}
