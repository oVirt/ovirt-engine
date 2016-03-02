package org.ovirt.engine.core.bll.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;

public class OvfUtilsTest {
    private static final String VM_OVF_XML_DATA = "backend/manager/modules/bll/src/test/resources/vmOvfData.xml";

    @Test
    public void testFetchVmDisks() throws IOException {
        try {
            XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
            List<Guid> disks = OvfUtils.fetchVmDisks(xmlDocument);
            assertNotNull("The list of disks should not be null", disks);
            assertTrue("The list of disks should not be empty", !disks.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateUnregisteredDisksWithVMsWithEmptyUnregDisks() throws IOException {
        try {
            XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
            List<UnregisteredDisk> unregDisks = new ArrayList<>();
            OvfUtils.updateUnregisteredDisksWithVMs(unregDisks, Guid.newGuid(), "TestVM", xmlDocument);
            assertTrue("The list of disks should not be empty", unregDisks.isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdateUnregisteredDisksWithVMsWithInitializedUnregDisks() throws IOException {
        try {
            XmlDocument xmlDocument = new XmlDocument(getXmlOvfData());
            List<UnregisteredDisk> unregDisks = new ArrayList<>();
            UnregisteredDisk unregDisk = new UnregisteredDisk();
            unregDisk.getDiskImage().setId(Guid.createGuidFromString("f934b12c-1e22-4ad8-bbce-ec0b2a5defa4"));
            unregDisk.getDiskImage()
                    .setStorageIds(new ArrayList<>(Collections.singletonList(Guid.createGuidFromString("7e2a7eac-3b76-4d45-a7dd-caae8fe0f588"))));
            unregDisks.add(unregDisk);
            OvfUtils.updateUnregisteredDisksWithVMs(unregDisks, Guid.newGuid(), "TestVM", xmlDocument);
            assertTrue("The list of disks should not be empty", !unregDisks.isEmpty());
            assertTrue("The VMs id is set in the unregisteterd disks", !unregDisks.get(0).getVms().isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getXmlOvfData() throws IOException {
        return new String(Files.readAllBytes(Paths.get(VM_OVF_XML_DATA)), StandardCharsets.UTF_8);
    }

}
