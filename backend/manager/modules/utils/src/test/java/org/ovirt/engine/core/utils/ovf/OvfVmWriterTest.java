package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
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
        vm.getStaticData().setdomain("domain_name");
        vm.setTimeZone("Israel Standard Time");
        initInterfaces(vm);
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
        Assert.assertEquals(vm.getStaticData(), newVm.getStaticData());
        Assert.assertEquals(vm.getInterfaces(), newVm.getInterfaces());
    }

    private void initInterfaces(VM vm) {
        VmNetworkInterface vmInterface = new VmNetworkInterface();
        vmInterface.setStatistics(new VmNetworkStatistics());
        vmInterface.setId(Guid.NewGuid());
        vmInterface.setName("eth77");
        vmInterface.setNetworkName("blue");
        vmInterface.setLinked(false);
        vmInterface.setSpeed(1000);
        vmInterface.setType(3);
        vmInterface.setMacAddress("01:C0:81:21:71:17");

        VmNetworkInterface vmInterface2 = new VmNetworkInterface();
        vmInterface2.setStatistics(new VmNetworkStatistics());
        vmInterface2.setId(Guid.NewGuid());
        vmInterface2.setName("eth88");
        vmInterface2.setNetworkName(null);
        vmInterface2.setLinked(true);
        vmInterface2.setSpeed(1234);
        vmInterface2.setType(1);
        vmInterface2.setMacAddress("02:C1:92:22:25:28");

        vm.setInterfaces(Arrays.asList(vmInterface, vmInterface2));
    }
}
