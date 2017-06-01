package org.ovirt.engine.core.bll.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;

public class OvfUtilsTest {
    private static final String VM_OVF_XML_DATA = "src/test/resources/vmOvfData.xml";

    @Test
    public void testFetchVmDisks() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        Set<Guid> disks = OvfUtils.fetchVmDisks(xmlDocument);
        assertNotNull("The list of disks should not be null", disks);
        assertTrue("The list of disks should not be empty", !disks.isEmpty());
    }

    @Test
    public void testIsExternalVM() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        assertFalse("VM should not be external VM", OvfUtils.isExternalVM(xmlDocument));
    }

    @Test
    public void testUpdateUnregisteredDisksWithVMsWithEmptyUnregDisks() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        List<UnregisteredDisk> unregDisks = new ArrayList<>();
        OvfUtils.updateUnregisteredDisksWithVMs(unregDisks, Guid.newGuid(), "TestVM", xmlDocument);
        assertTrue("The list of disks should not be empty", unregDisks.isEmpty());
    }

    @Test
    public void testUpdateUnregisteredDisksWithVMsWithInitializedUnregDisks() throws Exception {
        XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
        List<UnregisteredDisk> unregDisks = new ArrayList<>();
        UnregisteredDisk unregDisk = new UnregisteredDisk();
        unregDisk.getDiskImage().setId(Guid.createGuidFromString("8c634412-1e8b-4ef3-bc40-b67a456e9d2f"));
        unregDisk.getDiskImage()
                .setStorageIds(new ArrayList<>(Collections.singletonList(Guid.createGuidFromString("7e2a7eac-3b76-4d45-a7dd-caae8fe0f588"))));
        unregDisks.add(unregDisk);
        OvfUtils.updateUnregisteredDisksWithVMs(unregDisks, Guid.newGuid(), "TestVM", xmlDocument);
        assertTrue("The list of disks should not be empty", !unregDisks.isEmpty());
        assertTrue("The VMs id is set in the unregisteterd disks", !unregDisks.get(0).getVms().isEmpty());
    }

    private String getXmlOvfData() throws IOException {
        return new String(Files.readAllBytes(Paths.get(VM_OVF_XML_DATA)), StandardCharsets.UTF_8);
    }

}
