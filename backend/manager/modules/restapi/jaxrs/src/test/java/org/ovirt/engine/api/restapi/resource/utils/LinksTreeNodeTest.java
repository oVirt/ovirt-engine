package org.ovirt.engine.api.restapi.resource.utils;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.api.model.Vm;

public class LinksTreeNodeTest extends Assert{

    @Test
    public void testMarkAsFollowed() {
        LinkFollower linkFollower = new LinkFollower();
        LinksTreeNode linksTree = linkFollower.createLinksTree(Vm.class, "disk_attachments.disk,disk_attachments.template,tags,nics.network_labels");
        linksTree.markAsFollowed("disk_attachments.template");
        assertTrue(linksTree.getChild("disk_attachments").get().isFollowed());
        assertTrue(linksTree.getChild("disk_attachments").get().getChild("template").get().isFollowed());
        assertFalse(linksTree.getChild("nics").get().isFollowed());
        assertFalse(linksTree.getChild("disk_attachments").get().getChild("disk").get().isFollowed());
        linksTree.markAsFollowed("tags");
        assertTrue(linksTree.getChild("tags").get().isFollowed());
    }
}
