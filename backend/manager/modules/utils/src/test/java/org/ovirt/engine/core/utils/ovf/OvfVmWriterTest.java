package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.utils.MockConfigRule;

public class OvfVmWriterTest {

    @ClassRule
    public static MockConfigRule mockConfigRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.VdcVersion, "1.0.0.0"));

    @Test
    public void test() {
        final VM vm = new VM();
        vm.setVmName("test-vm");
        vm.setOrigin(OriginType.OVIRT);
        vm.setId(new Guid());
        vm.setVmDescription("test-description");
        final XmlDocument document = new XmlDocument();
        OvfVmWriter vmWriter = new OvfVmWriter(document, vm, new ArrayList<DiskImage>());
        vmWriter.BuildReference();
        vmWriter.BuildNetwork();
        vmWriter.BuildDisk();
        vmWriter.BuildVirtualSystem();

        vmWriter.dispose();

        Assert.assertNotNull(document.OuterXml);

        // test it be deserializing

        final VM newVm = new VM();
        OvfVmReader vmReader =
                new OvfVmReader(document, newVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
        vmReader.BuildReference();
        vmReader.BuildNetwork();
        vmReader.BuildDisk();
        vmReader.BuildVirtualSystem();

        Assert.assertEquals(vm, newVm);
    }
}
