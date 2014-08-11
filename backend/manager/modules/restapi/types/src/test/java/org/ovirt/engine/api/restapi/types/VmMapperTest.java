package org.ovirt.engine.api.restapi.types;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.GuestNicConfiguration;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.VCpuPin;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmDeviceType;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPayload;
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

    @BeforeClass
    public static void beforeClass() {
        OsTypeMockUtils.mockOsTypes();
    }

    @Override
    protected void setUpConfigExpectations() {
        mcr.mockConfigValue(ConfigValues.NumberVmRefreshesBeforeSave, 10);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getInverse(VmStatic to) {
        VmStatistics statistics = new VmStatistics();
        statistics.setcpu_user(10.0);
        statistics.setcpu_sys(20.0);
        VmDynamic dynamic = new VmDynamic();
        dynamic.setStatus(VMStatus.Up);
        dynamic.setBootSequence(to.getDefaultBootSequence());
        dynamic.setDisplayType(to.getDefaultDisplayType());
        org.ovirt.engine.core.common.businessentities.VM ret =
                new org.ovirt.engine.core.common.businessentities.VM(to,
                        dynamic,
                        statistics);
        ret.setUsageMemPercent(50);
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
        CpuTune cpuTune = new CpuTune();
        VCpuPin pin = new VCpuPin();
        pin.setVcpu(33);
        pin.setCpuSet("1-4,6");
        cpuTune.getVCpuPin().add(pin);
        from.getCpu().setCpuTune(cpuTune);
        from.setTimezone("Australia/Darwin");
        from.setPlacementPolicy(new VmPlacementPolicy());
        from.getPlacementPolicy().setHost(new Host());
        from.getPlacementPolicy().getHost().setId(Guid.Empty.toString());
        for (GuestNicConfiguration guestNic : from.getInitialization().getNicConfigurations().getNicConfigurations()) {
            guestNic.setBootProtocol(MappingTestHelper.shuffle(BootProtocol.class).value());
        }
        from.getSerialNumber().setPolicy(SerialNumberPolicy.CUSTOM.value());
        from.getDisplay().setFileTransferEnabled(true);
        from.getDisplay().setCopyPasteEnabled(true);
        return from;
    }

    @Override
    protected void verify(VM model, VM transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getComment(), transform.getComment());
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
        assertEquals(model.getBios().getBootMenu().isEnabled(), transform.getBios().getBootMenu().isEnabled());
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
        assertEquals(model.getDisplay().isSingleQxlPci(), transform.getDisplay().isSingleQxlPci());
        assertEquals(model.getDisplay().isAllowOverride(), transform.getDisplay().isAllowOverride());
        assertEquals(model.getPlacementPolicy().getHost().getId(), transform.getPlacementPolicy().getHost().getId());
        assertTrue(Math.abs(model.getMemoryPolicy().getGuaranteed() - transform.getMemoryPolicy().getGuaranteed()) <= (1024 * 1024));
        assertEquals(model.getTimezone(), transform.getTimezone());
        assertEquals(model.getDisplay().isSmartcardEnabled(), transform.getDisplay().isSmartcardEnabled());
        assertEquals(model.getDisplay().getKeyboardLayout(), transform.getDisplay().getKeyboardLayout());
        assertEquals(model.isDeleteProtected(), transform.isDeleteProtected());
        assertEquals(model.isTunnelMigration(), transform.isTunnelMigration());
        assertEquals(model.getMigrationDowntime(), transform.getMigrationDowntime());
        assertEquals(model.getSerialNumber().getPolicy(), transform.getSerialNumber().getPolicy());
        assertEquals(model.getSerialNumber().getValue(), transform.getSerialNumber().getValue());
        assertEquals(model.getDisplay().isFileTransferEnabled(), transform.getDisplay().isFileTransferEnabled());
        assertEquals(model.getDisplay().isCopyPasteEnabled(), transform.getDisplay().isCopyPasteEnabled());
    }

    @Test
    public void testVmPayloadMapToPaylod() {
        VmPayload vmPayload = new VmPayload();
        vmPayload.setType(org.ovirt.engine.core.common.utils.VmDeviceType.CDROM);
        vmPayload.setVolumeId("CD-VOL");
        Payload payload = VmMapper.map(vmPayload, null);
        assertEquals(vmPayload.getType().name().toLowerCase(), payload.getType());
        assertEquals(vmPayload.getVolumeId(), payload.getVolumeId());
    }

    @Test
    public void testPayloadMapToVmPaylod() {
        Payload payload = new Payload();
        payload.setType("CDROM");
        payload.setVolumeId("CD-VOL");
        VmPayload vmPayload = VmMapper.map(payload, null);
        assertEquals(payload.getType(), vmPayload.getType().name());
        assertEquals(payload.getVolumeId(), vmPayload.getVolumeId());
    }


    @Test
    public void ovfConfigurationMap() {
        String ovfConfig = "config";
        ConfigurationType configurationType = ConfigurationType.OVF;
        VM model = new VM();
        VmMapper.map(ovfConfig, ConfigurationType.OVF, model);
        assertNotNull(model.getInitialization());
        assertNotNull(model.getInitialization().getConfiguration());
        assertEquals(model.getInitialization().getConfiguration().getData(), ovfConfig);
        assertEquals(ConfigurationType.fromValue(model.getInitialization().getConfiguration().getType()),
                configurationType);
    }

    @Test
    public void testGustIp() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);
        vmDynamic.setVmIp("2.2.2.2");
        vm.setDynamicData(vmDynamic);
        VM map = VmMapper.map(vm, (VM) null);
        assertNotNull(map.getGuestInfo().getIps().getIPs().get(0));
        assertEquals(map.getGuestInfo().getIps().getIPs().get(0).getAddress(), "2.2.2.2");
    }

    @Test
    public void testGuestFQDN() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);
        vmDynamic.setVmFQDN("localhost.localdomain");
        vm.setDynamicData(vmDynamic);
        VM map = VmMapper.map(vm, (VM) null);
        assertNotNull(map.getGuestInfo().getFqdn());
        assertEquals(map.getGuestInfo().getFqdn(), "localhost.localdomain");
    }
    @Test
    public void testGustIps() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);
        vmDynamic.setVmIp("2.2.2.2 2.2.2.3 2.2.2.4");
        vm.setDynamicData(vmDynamic);
        VM map = VmMapper.map(vm, (VM) null);
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
        VM model = VmMapper.map(entity, (VM) null);
        assertTrue(model.getDisplay().getPort() == 5900);
        assertTrue(model.getDisplay().getSecurePort() == 9999);
        entity.setDisplay(-1);
        entity.setDisplaySecurePort(-1);
        model = VmMapper.map(entity, (VM) null);
        assertNull(model.getDisplay().getPort());
        assertNull(model.getDisplay().getSecurePort());
    }

    private static final String GLOBAL_SPICE_PROXY = "http://host:12345";
    private static final String CLUSTER_SPICE_PROXY = "http://host2:54321";
    private static final String POOL_SPICE_PROXY = "http://host3:9999";

    @Test
    public void testGlobalSpiceProxy() {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        mcr.mockConfigValue(ConfigValues.SpiceProxyDefault, GLOBAL_SPICE_PROXY);
        VM model = VmMapper.map(entity, (VM) null);
        assertEquals(GLOBAL_SPICE_PROXY, model.getDisplay().getProxy());
    }

    @Test
    public void testClusterSpiceProxy() {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        entity.setVdsGroupSpiceProxy(CLUSTER_SPICE_PROXY);
        VM model = VmMapper.map(entity, (VM) null);
        assertEquals(CLUSTER_SPICE_PROXY, model.getDisplay().getProxy());
    }

    @Test
    public void testPoolSpiceProxy() {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        entity.setVmPoolSpiceProxy(POOL_SPICE_PROXY);
        VM model = VmMapper.map(entity, (VM) null);
        assertEquals(POOL_SPICE_PROXY, model.getDisplay().getProxy());
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
        Guid guid = Guid.newGuid();
        entity.setRunOnVds(guid);
        VM model = VmMapper.map(entity, (VM) null);
        assertEquals(guid.toString(), model.getHost().getId());
    }

    @Test
    public void testMapConfigurationType() {
        org.ovirt.engine.core.common.businessentities.ConfigurationType configurationTypeBll = VmMapper.map(ConfigurationType.OVF, null);
        assertEquals(configurationTypeBll, org.ovirt.engine.core.common.businessentities.ConfigurationType.OVF);

        ConfigurationType configurationTypeApi = VmMapper.map(org.ovirt.engine.core.common.businessentities.ConfigurationType.OVF, null);
        assertEquals(configurationTypeApi, ConfigurationType.OVF);
    }

    @Test
    public void stringToCpuTune() {
        CpuTune cpuTune = VmMapper.stringToCpuTune("0#0");
        assertNotNull(cpuTune);
        assertNotNull(cpuTune.getVCpuPin());
        assertEquals(1, cpuTune.getVCpuPin().size());
        assertEquals(0, (int) cpuTune.getVCpuPin().get(0).getVcpu());
        assertEquals("0", cpuTune.getVCpuPin().get(0).getCpuSet());
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
        assertEquals(1, (int) pin.getVcpu());
        assertEquals("1-4,6", pin.getCpuSet());
    }

    @Test()
    public void stringToVCpupinSimple() {
        VCpuPin pin = VmMapper.stringToVCpupin("1#1");
        assertEquals(1, (int) pin.getVcpu());
        assertEquals("1", pin.getCpuSet());

        pin = VmMapper.stringToVCpupin("1#10");
        assertEquals(1, (int) pin.getVcpu());
        assertEquals("10", pin.getCpuSet());

        pin = VmMapper.stringToVCpupin("1#10,11,12");
        assertEquals(1, (int) pin.getVcpu());
        assertEquals("10,11,12", pin.getCpuSet());

        pin = VmMapper.stringToVCpupin("1#10-12,16");
        assertEquals(1, (int) pin.getVcpu());
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
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup.getcompatibility_version()), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyIsSetDisabled() {
        Usb usb = new Usb();
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup.getcompatibility_version()), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicySetDisabled() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup.getcompatibility_version()), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyUsbTypeNative() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("native");
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup.getcompatibility_version()), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyUsbTypeLegacy() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("legacy");
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup.getcompatibility_version()), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyUsbTypeNull31() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_1);
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup.getcompatibility_version()), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyUsbTypeNull30() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_0);
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb, vdsGroup.getcompatibility_version()), UsbPolicy.ENABLED_LEGACY);
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
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, vdsGroup.getcompatibility_version()), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledPolicyNotSetUsbOnPost3_0Cluster() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_1);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED, vdsGroup.getcompatibility_version()), UsbPolicy.ENABLED_NATIVE);
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

    @Test
    public void testMapSessions() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setConsoleCurrentUserName("admin");
        vmDynamic.setClientIp("1.1.1.1");
        vmDynamic.setGuestCurrentUserName("Ori");
        vm.setDynamicData(vmDynamic);
        Sessions sessions = VmMapper.map(vm, new Sessions());
        assertNotNull(sessions);
        assertEquals(sessions.getSessions().size(), 2);
        Session consoleSession =
                sessions.getSessions().get(0).getUser().getUserName().equals("admin") ? sessions.getSessions().get(0)
                        : sessions.getSessions().get(1);
        Session guestSession =
                sessions.getSessions().get(0).getUser().getUserName().equals("Ori") ? sessions.getSessions().get(0)
                        : sessions.getSessions().get(1);
        assertEquals(consoleSession.getUser().getUserName(), "admin");
        assertEquals(consoleSession.getIp().getAddress(), "1.1.1.1");
        assertTrue(consoleSession.isConsoleUser());
        assertEquals(guestSession.getUser().getUserName(), "Ori");
    }
}
