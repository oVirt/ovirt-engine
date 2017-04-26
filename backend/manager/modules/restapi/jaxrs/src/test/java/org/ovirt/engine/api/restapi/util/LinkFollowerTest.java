package org.ovirt.engine.api.restapi.util;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.api.model.ActionableResource;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.api.model.DiskAttachments;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.restapi.resource.BackendVmNicsResource;
import org.ovirt.engine.api.restapi.resource.ResourceLocator;
import org.ovirt.engine.api.restapi.resource.utils.LinkFollower;
import org.ovirt.engine.api.restapi.resource.utils.LinksTreeNode;

@RunWith(MockitoJUnitRunner.class)
public class LinkFollowerTest extends Assert {

    private LinkFollower linkFollower;

    @Mock
    private ResourceLocator resourceLocator;

    @Mock BackendVmNicsResource vmNicsResource;

    @Before
    public void setUp() {
        linkFollower = new LinkFollower(resourceLocator) {
            //override fetch() since it requires a real environment and would crash tests.
            protected ActionableResource fetch(String href) {
                if (href.equals("/ovirt-engine/api/vms/63978315-2d17-4e67-b393-2ea60a8aeacb/nics")){
                    return createNics();
                }
                else if (href.equals("/ovirt-engine/api/vms/63978315-2d17-4e67-b393-2ea60a8aeacb/diskattachments")) {
                    return createDiskAttachments();
                }
                else if (href.equals("/ovirt-engine/api/disks/aaa")) {
                    return new Disk();
                }
                else if (href.equals("/ovirt-engine/api/disks/bbb")) {
                    return new Disk();
                }
                else if (href.equals("/ovirt-engine/api/disks/ccc")) {
                    return new Disk();
                }
                else {
                    return null;
                }
            }


        };
    }

    @Test
    public void testFollowLinks() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        LinksTreeNode linksTree = linkFollower.createLinksTree(Vm.class, "nics,disk_attachments.disk");
        Vm vm = createVm();
        linkFollower.followLinks(vm, linksTree);
        assertNotNull(vm.getNics());
        assertNotNull(vm.getNics().getNics());
        assertFalse(vm.getNics().getNics().isEmpty());
        assertEquals(2, vm.getNics().getNics().size());
        assertNotNull(vm.getDiskAttachments());
        assertNotNull(vm.getDiskAttachments().getDiskAttachments());
        assertFalse(vm.getDiskAttachments().getDiskAttachments().isEmpty());
        assertEquals(3, vm.getDiskAttachments().getDiskAttachments().size());
        assertNotNull(vm.getDiskAttachments().getDiskAttachments().get(0).getDisk());
        assertNotNull(vm.getDiskAttachments().getDiskAttachments().get(1).getDisk());
        assertNotNull(vm.getDiskAttachments().getDiskAttachments().get(2).getDisk());
    }

    private Vm createVm() {
        Vm vm = new Vm();
        //add an irrelevant link
        Link disksLink = new Link();
        disksLink.setHref("/ovirt-engine/api/vms/63978315-2d17-4e67-b393-2ea60a8aeacb/diskattachments");
        disksLink.setRel("diskattachments");
        vm.getLinks().add(disksLink);
        Link nicsLink = new Link();
        nicsLink.setHref("/ovirt-engine/api/vms/63978315-2d17-4e67-b393-2ea60a8aeacb/nics");
        nicsLink.setRel("nics");
        vm.getLinks().add(nicsLink);
        return vm;
    }

    private Nics createNics() {
        Nics nics = new Nics();
        Nic nic1 = new Nic();
        nic1.setId("aaa");
        nics.getNics().add(nic1);
        Nic nic2 = new Nic();
        nic1.setId("bbb");
        nics.getNics().add(nic2);
        return nics;
    }

    private DiskAttachments createDiskAttachments() {
        DiskAttachments disks = new DiskAttachments();
        DiskAttachment diskAttachment1 = new DiskAttachment();
        DiskAttachment diskAttachment2 = new DiskAttachment();
        DiskAttachment diskAttachment3 = new DiskAttachment();
        Disk disk1 = new Disk();
        disk1.setId("aaa");
        disk1.setHref("/ovirt-engine/api/disks/aaa");
        Disk disk2 = new Disk();
        disk2.setId("bbb");
        disk2.setHref("/ovirt-engine/api/disks/bbb");
        Disk disk3 = new Disk();
        disk3.setId("ccc");
        disk3.setHref("/ovirt-engine/api/disks/ccc");
        diskAttachment1.setDisk(disk1);
        diskAttachment2.setDisk(disk2);
        diskAttachment3.setDisk(disk3);
        disks.getDiskAttachments().add(diskAttachment1);
        disks.getDiskAttachments().add(diskAttachment2);
        disks.getDiskAttachments().add(diskAttachment3);
        return disks;
    }

    @Test
    public void testCreateLinksTree() {
        LinksTreeNode linksTree = linkFollower.createLinksTree(Vm.class, "disk_attachments.disk,disk_attachments.template,tags,nics.network_labels");
        assertNotNull(linksTree);
        assertEquals("vm", linksTree.getElement());
        assertTrue(linksTree.isRoot());
        assertFalse(linksTree.isFollowed());
        assertEquals(3, linksTree.getChildren().size());

        //disk_attachments
        LinksTreeNode child1 = linksTree.getChild("disk_attachments").get();
        assertNotNull(child1);
        assertFalse(child1.isRoot());
        assertFalse(child1.isFollowed());
        assertEquals("disk_attachments", child1.getElement());
        assertEquals(child1.getChildren().size(), 2);
        assertNotNull(child1.getChild("disk").get());
        assertNotNull(child1.getChild("template").get());

        //nics
        LinksTreeNode child2 = linksTree.getChild("nics").get();
        assertNotNull(child2);
        assertFalse(child2.isRoot());
        assertFalse(child2.isFollowed());
        assertEquals("nics", child2.getElement());
        assertEquals(child2.getChildren().size(), 1);
        assertNotNull(child2.getChild("network_labels").get());

        //tags
        assertNotNull(linksTree.getChild("tags").get());
    }

}

