package org.ovirt.engine.api.restapi.types;

import junit.framework.Assert;

import org.junit.Test;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.VCpuPin;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmDeviceType;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmMapperTest extends
        AbstractInvertibleMappingTest<VM, VmStatic, org.ovirt.engine.core.common.businessentities.VM> {

    public VmMapperTest() {
        super(VM.class, VmStatic.class, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    protected void setUpConfigExpectations() {
        mcr.mockConfigValue(ConfigValues.NumberVmRefreshesBeforeSave, 10);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getInverse(VmStatic to) {
        VmStatistics statistics = new VmStatistics();
        statistics.setcpu_user(new Double(10L));
        statistics.setcpu_sys(new Double(20L));
        VmDynamic dynamic = new VmDynamic();
        dynamic.setStatus(VMStatus.Up);
        dynamic.setBootSequence(to.getDefaultBootSequence());
        dynamic.setDisplayType(to.getDefaultDisplayType());
        org.ovirt.engine.core.common.businessentities.VM ret =
                new org.ovirt.engine.core.common.businessentities.VM(to,
                        dynamic,
                        statistics);
        ret.setUsageMemPercent(Integer.valueOf(50));
        return ret;
    }

    @Override
    protected VM postPopulate(VM from) {
        from.setType(MappingTestHelper.shuffle(VmType.class).value());
        from.setOrigin(OriginType.VMWARE.name().toLowerCase());
        from.getDisplay().setType(MappingTestHelper.shuffle(DisplayType.class).value());
        from.getPayloads().getPayload().get(0).setType(MappingTestHelper.shuffle(VmDeviceType.class).value());
        for (Boot boot : from.getOs().getBoot()) {
            boot.setDev(MappingTestHelper.shuffle(BootDevice.class).value());
        }
        while (from.getCpu().getTopology().getSockets() == 0) {
            from.getCpu().getTopology().setSockets(MappingTestHelper.rand(100));
        }
        while (from.getCpu().getTopology().getCores() == 0) {
            from.getCpu().getTopology().setCores(MappingTestHelper.rand(100));
        }
        from.setTimezone("Australia/Darwin");
        return from;
    }

    @Override
    protected void verify(VM model, VM transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.getOrigin(), transform.getOrigin());
        assertTrue(Math.abs(model.getMemory() - transform.getMemory()) <= (1024 * 1024));
        assertNotNull(transform.getTemplate());
        assertEquals(model.getTemplate().getId(), transform.getTemplate().getId());
        assertNotNull(transform.getCluster());
        assertNotNull(transform.getCpu());
        assertNotNull(transform.getCpu().getTopology());
        assertTrue(Math.abs(model.getCpu().getTopology().getCores() -
                transform.getCpu().getTopology().getCores()) < model.getCpu().getTopology().getSockets());
        assertEquals(model.getCpu().getTopology().getSockets(),
                transform.getCpu().getTopology().getSockets());
        assertNotNull(transform.getOs());
        assertTrue(transform.getOs().isSetBoot());
        assertEquals(model.getOs().getBoot().size(), transform.getOs().getBoot().size());
        for (int i = 0; i < model.getOs().getBoot().size(); i++) {
            assertEquals(model.getOs().getBoot().get(i).getDev(), transform.getOs().getBoot()
                    .get(i).getDev());
        }
        assertEquals(model.getOs().getKernel(), transform.getOs().getKernel());
        assertEquals(model.getOs().getInitrd(), transform.getOs().getInitrd());
        assertEquals(model.getOs().getCmdline(), transform.getOs().getCmdline());
        assertTrue(transform.isSetDisplay());
        assertEquals(model.isSetDisplay(), transform.isSetDisplay());
        assertEquals(model.getDisplay().getType(), transform.getDisplay().getType());
        assertEquals(model.getDisplay().getMonitors(), transform.getDisplay().getMonitors());
        assertEquals(model.getDisplay().isAllowOverride(), transform.getDisplay().isAllowOverride());
        assertEquals(model.getPlacementPolicy().getHost().getId(), transform.getPlacementPolicy().getHost().getId());
        assertTrue(Math.abs(model.getMemoryPolicy().getGuaranteed() - transform.getMemoryPolicy().getGuaranteed()) <= (1024 * 1024));
        assertEquals(model.getDomain().getName(), transform.getDomain().getName());
        assertEquals(model.getTimezone(), transform.getTimezone());
        assertEquals(model.getDisplay().isSmartcardEnabled(), transform.getDisplay().isSmartcardEnabled());
        assertEquals(model.getDisplay().getKeyboardLayout(), transform.getDisplay().getKeyboardLayout());
        assertEquals(model.isDeleteProtected(), transform.isDeleteProtected());
        assertEquals(model.isTunnelMigration(), transform.isTunnelMigration());
    }

    @Test
    public void testGustIp() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);
        vmDynamic.setVmIp("2.2.2.2");
        vm.setDynamicData(vmDynamic);
        OsTypeMockUtils.mockOsTypes();
        VM map = VmMapper.map(vm, null);
        assertNotNull(map.getGuestInfo().getIps().getIPs().get(0));
        assertEquals(map.getGuestInfo().getIps().getIPs().get(0).getAddress(), "2.2.2.2");
    }

    @Test
    public void testGustIps() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);
        vmDynamic.setVmIp("2.2.2.2 2.2.2.3 2.2.2.4");
        vm.setDynamicData(vmDynamic);
        OsTypeMockUtils.mockOsTypes();
        VM map = VmMapper.map(vm, null);
        assertNotNull(map.getGuestInfo().getIps().getIPs().get(0));
        assertEquals(map.getGuestInfo().getIps().getIPs().get(0).getAddress(), "2.2.2.2");
        assertEquals(map.getGuestInfo().getIps().getIPs().get(1).getAddress(), "2.2.2.3");
        assertEquals(map.getGuestInfo().getIps().getIPs().get(2).getAddress(), "2.2.2.4");
    }

    @Test
    public void testDisplayPort() {
        org.ovirt.engine.core.common.businessentities.VM entity =
                new org.ovirt.engine.core.common.businessentities.VM();
        entity.setStatus(VMStatus.Up);
        entity.setDisplay(5900);
        entity.setDisplaySecurePort(9999);
        VM model = VmMapper.map(entity, null);
        assertTrue(model.getDisplay().getPort() == 5900);
        assertTrue(model.getDisplay().getSecurePort() == 9999);
        entity.setDisplay(-1);
        entity.setDisplaySecurePort(-1);
        model = VmMapper.map(entity, null);
        assertNull(model.getDisplay().getPort());
        assertNull(model.getDisplay().getSecurePort());
    }

    @Test
    public void testMapOriginTypeRhev() {
        String s = VmMapper.map(OriginType.RHEV, null);
        assertEquals(s, "rhev");
        OriginType s2 = VmMapper.map(s, OriginType.RHEV);
        assertEquals(s2, OriginType.RHEV);
    }

    @Test
    public void testMapOriginTypeOvirt() {
        String s = VmMapper.map(OriginType.OVIRT, null);
        assertEquals(s, "ovirt");
        OriginType s2 = VmMapper.map(s, OriginType.OVIRT);
        assertEquals(s2, OriginType.OVIRT);
    }

    @Test
    public void testMapHostId() {
        org.ovirt.engine.core.common.businessentities.VM entity =
                new org.ovirt.engine.core.common.businessentities.VM();
        entity.setStatus(VMStatus.Up);
        Guid guid = Guid.NewGuid();
        entity.setRunOnVds(guid);
        VM model = VmMapper.map(entity, null);
        assertEquals(guid.toString(), model.getHost().getId());
    }

    @Test
    public void stringToCpuTune() {
        CpuTune cpuTune = VmMapper.stringToCpuTune("0#0");
        assertNotNull(cpuTune);
        assertNotNull(cpuTune.getVcpuPin());
        assertEquals(1, cpuTune.getVcpuPin().size());
        assertEquals(0, cpuTune.getVcpuPin().get(0).getVcpu());
        assertEquals("0", cpuTune.getVcpuPin().get(0).getCpuSet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void stringToVCpupinBadCpuNumber() {
        VmMapper.stringToVCpupin("XXX#1-4");
    }

    @Test(expected = IllegalArgumentException.class)
    public void stringToVCpupinWrongFormat() {
        VmMapper.stringToVCpupin("X#X#X");
    }

    @Test()
    public void stringToVCpupinIntervalsList() {
        VCpuPin pin = VmMapper.stringToVCpupin("1#1-4,6");
        assertEquals(1, pin.getVcpu());
        assertEquals("1-4,6", pin.getCpuSet());
    }

    @Test()
    public void stringToVCpupinSimple() {
        VCpuPin pin = VmMapper.stringToVCpupin("1#1");
        assertEquals(1, pin.getVcpu());
        assertEquals("1", pin.getCpuSet());

        pin = VmMapper.stringToVCpupin("1#10");
        assertEquals(1, pin.getVcpu());
        assertEquals("10", pin.getCpuSet());

        pin = VmMapper.stringToVCpupin("1#10,11,12");
        assertEquals(1, pin.getVcpu());
        assertEquals("10,11,12", pin.getCpuSet());

        pin = VmMapper.stringToVCpupin("1#10-12,16");
        assertEquals(1, pin.getVcpu());
        assertEquals("10-12,16", pin.getCpuSet());
    }

    @Test
    public void cpuTuneToString() {
        for (String sample : new String[] { "0#0", "0#0_1#1", "0#0_1#1,2,3,6" }) {
            Assert.assertEquals(sample, VmMapper.cpuTuneToString(VmMapper.stringToCpuTune(sample)));
        }
    }

    @Test
    public void getUsbPolicyNullUsb() {
        Usb usb = null;
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyIsSetDisabled() {
        Usb usb = new Usb();
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicySetDisabled() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyUsbTypeNative() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("native");
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyUsbTypeLegacy() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("legacy");
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyUsbTypeNull31() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_1);
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyUsbTypeNull30() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_0);
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotNullUsb() {
        Usb usb = null;
        UsbPolicy currentPolicy = UsbPolicy.DISABLED;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy, null), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotNullUsb() {
        Usb usb = null;
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_LEGACY;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy, null), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotNullUsb() {
        Usb usb = null;
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_NATIVE;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy, null), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNotSetPolicyNotSetUsb() {
        Usb usb = new Usb();
        UsbPolicy currentPolicy = UsbPolicy.DISABLED;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy, null), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotEnabledNotSetPolicyNotSetUsb() {
        Usb usb = new Usb();
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_LEGACY;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy, null), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNotSetPolicyNotSetUsb() {
        Usb usb = new Usb();
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_NATIVE;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy, null), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNotSetLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, null), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNotSetNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setType("native");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, null), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotEnabledNotSetLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_LEGACY, null), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNotSetLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE, null), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotEnabledNotSetNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setType("native");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_LEGACY, null), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNotSetNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setType("native");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE, null), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotDisabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, null), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotDisabledNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType("native");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, null), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotDisabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_LEGACY, null), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotDisabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE, null), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledPolicyNotSetUsbOnPre3_1Cluster() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_0);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, vdsGroup), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledPolicyNotSetUsbOnPost3_0Cluster() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_1);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, vdsGroup), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotEnabledPolicyNotSetUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_LEGACY, null), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledPolicyNotSetUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE, null), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE, null), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("legacy");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, null), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("native");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE, null), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("native");
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, null), UsbPolicy.ENABLED_NATIVE);
    }
}
