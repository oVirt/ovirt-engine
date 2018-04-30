package org.ovirt.engine.core.bll.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;

public class OvfUtilsTest {
    private static final String VM_OVF_XML_DATA = "src/test/resources/vmOvfData.xml";

    private OvfUtils ovfUtils;

    @BeforeEach
    public void setUp() {
        ovfUtils = new OvfUtils();
    }

    @Test
    public void testFetchVmDisks() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        Set<Guid> disks = ovfUtils.fetchVmDisks(xmlDocument);
        assertNotNull(disks, "The list of disks should not be null");
        assertTrue(!disks.isEmpty(), "The list of disks should not be empty");
    }

    @Test
    public void testIsExternalVM() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        assertFalse(ovfUtils.isExternalVM(xmlDocument), "VM should not be external VM");
    }

    @Test
    public void testUpdateUnregisteredDisksWithVMsWithEmptyUnregDisks() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        List<UnregisteredDisk> unregDisks = new ArrayList<>();
        ovfUtils.updateUnregisteredDisksWithVMs(unregDisks, Guid.newGuid(), "TestVM", xmlDocument);
        assertTrue(unregDisks.isEmpty(), "The list of disks should not be empty");
    }

    @Test
    public void testMemoryDisks() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        Set<Guid> memoryDisks = ovfUtils.fetchMemoryDisks(xmlDocument);
        assertFalse(memoryDisks.isEmpty(), "The list of memory disks for snapshot should not be empty");
    }

    @Test
    public void testUpdateUnregisteredDisksWithVMsWithInitializedUnregDisks() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        List<UnregisteredDisk> unregDisks = new ArrayList<>();
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.createGuidFromString("8c634412-1e8b-4ef3-bc40-b67a456e9d2f"));
        diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(Guid.createGuidFromString("7e2a7eac-3b76-4d45-a7dd-caae8fe0f588"))));
        UnregisteredDisk unregDisk = new UnregisteredDisk(diskImage);

        unregDisks.add(unregDisk);
        ovfUtils.updateUnregisteredDisksWithVMs(unregDisks, Guid.newGuid(), "TestVM", xmlDocument);
        assertTrue(!unregDisks.isEmpty(), "The list of disks should not be empty");
        assertTrue(!unregDisks.get(0).getVms().isEmpty(), "The VMs id is set in the unregisteterd disks");
    }

    private String getXmlOvfData() throws IOException {
        return new String(Files.readAllBytes(Paths.get(VM_OVF_XML_DATA)), StandardCharsets.UTF_8);
    }

}
