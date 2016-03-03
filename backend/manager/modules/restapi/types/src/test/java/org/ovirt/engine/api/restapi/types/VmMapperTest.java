package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.ConfigurationType;
import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.DisplayDisconnectAction;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.NicConfiguration;
import org.ovirt.engine.api.model.Payload;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.api.model.Session;
import org.ovirt.engine.api.model.Sessions;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.api.model.VcpuPin;
import org.ovirt.engine.api.model.VcpuPins;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmDeviceType;
import org.ovirt.engine.api.model.VmPlacementPolicy;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

public class VmMapperTest extends
        AbstractInvertibleMappingTest<Vm, VmStatic, org.ovirt.engine.core.common.businessentities.VM> {

    public VmMapperTest() {
        super(Vm.class, VmStatic.class, org.ovirt.engine.core.common.businessentities.VM.class);
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
        statistics.setCpuUser(10.0);
        statistics.setCpuSys(20.0);
        VmDynamic dynamic = new VmDynamic();
        dynamic.setStatus(VMStatus.Up);
        dynamic.setBootSequence(to.getDefaultBootSequence());
        dynamic.getGraphicsInfos().put(GraphicsType.SPICE, new GraphicsInfo());
        org.ovirt.engine.core.common.businessentities.VM ret =
                new org.ovirt.engine.core.common.businessentities.VM(to,
                        dynamic,
                        statistics);
        ret.setUsageMemPercent(50);
        return ret;
    }

    @Override
    protected Vm postPopulate(Vm from) {
        from.setType(MappingTestHelper.shuffle(VmType.class));
        from.setOrigin(OriginType.VMWARE.name().toLowerCase());
        from.getDisplay().setType(MappingTestHelper.shuffle(DisplayType.class));
        from.getPayloads().getPayloads().get(0).setType(MappingTestHelper.shuffle(VmDeviceType.class));
        List<BootDevice> devices = from.getOs().getBoot().getDevices().getDevices();
        for (int i = 0; i < devices.size(); i++) {
            devices.set(i, MappingTestHelper.shuffle(BootDevice.class));
        }
        while (from.getCpu().getTopology().getSockets() == 0) {
            from.getCpu().getTopology().setSockets(MappingTestHelper.rand(100));
        }
        while (from.getCpu().getTopology().getCores() == 0) {
            from.getCpu().getTopology().setCores(MappingTestHelper.rand(100));
        }
        CpuTune cpuTune = new CpuTune();
        VcpuPin pin = new VcpuPin();
        pin.setVcpu(33);
        pin.setCpuSet("1-4,6");
        VcpuPins pins = new VcpuPins();
        pins.getVcpuPins().add(pin);
        cpuTune.setVcpuPins(pins);
        from.getCpu().setCpuTune(cpuTune);
        from.setTimeZone(new TimeZone());
        from.getTimeZone().setName("Australia/Darwin");
        // VmPlacement - multiple hosts
        from.setPlacementPolicy(createPlacementPolicy(Guid.EVERYONE, Guid.SYSTEM));
        // Guest Nics configurations
        for (NicConfiguration guestNic : from.getInitialization().getNicConfigurations().getNicConfigurations()) {
            guestNic.setBootProtocol(MappingTestHelper.shuffle(BootProtocol.class, BootProtocol.AUTOCONF));
        }
        from.getDisplay().setType(DisplayType.SPICE);
        from.getSerialNumber().setPolicy(SerialNumberPolicy.CUSTOM);
        from.getDisplay().setFileTransferEnabled(true);
        from.getDisplay().setCopyPasteEnabled(true);
        from.getMigration().setAutoConverge(InheritableBoolean.TRUE);
        from.getMigration().setCompressed(InheritableBoolean.TRUE);
        from.getDisplay().setDisconnectAction(DisplayDisconnectAction.LOCK_SCREEN.toString());
        return from;
    }

    private VmPlacementPolicy createPlacementPolicy(Guid... guids) {
        VmPlacementPolicy placementPolicy = new VmPlacementPolicy();
        Hosts hostsList = new Hosts();
        for (Guid guid : guids) {
            Host newHost = new Host();
            newHost.setId(guid.toString());
            hostsList.getHosts().add(newHost);
        }
        placementPolicy.setHosts(hostsList);
        return placementPolicy;
    }

    @Override
    protected void verify(Vm model, Vm transform) {
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
        assertEquals(model.getOs().getBoot().getDevices().getDevices(),
                transform.getOs().getBoot().getDevices().getDevices());
        assertEquals(model.getOs().getKernel(), transform.getOs().getKernel());
        assertEquals(model.getOs().getInitrd(), transform.getOs().getInitrd());
        assertEquals(model.getOs().getCmdline(), transform.getOs().getCmdline());
        assertTrue(transform.isSetDisplay());
        assertEquals(model.isSetDisplay(), transform.isSetDisplay());
        assertEquals(model.getDisplay().getType(), transform.getDisplay().getType());
        assertEquals(model.getDisplay().getMonitors(), transform.getDisplay().getMonitors());
        assertEquals(model.getDisplay().isSingleQxlPci(), transform.getDisplay().isSingleQxlPci());
        assertEquals(model.getDisplay().isAllowOverride(), transform.getDisplay().isAllowOverride());
        // few hosts in Placement Policy, but unordered
        List<Host> modelHostsList = model.getPlacementPolicy().getHosts().getHosts();
        List<Host> trnsfHostsList = transform.getPlacementPolicy().getHosts().getHosts();
        assertHostsListMatch(modelHostsList, trnsfHostsList);
        assertTrue(Math.abs(model.getMemoryPolicy().getGuaranteed() - transform.getMemoryPolicy().getGuaranteed()) <= (1024 * 1024));
        assertEquals(model.getTimeZone().getName(), transform.getTimeZone().getName());
        assertEquals(model.getDisplay().isSmartcardEnabled(), transform.getDisplay().isSmartcardEnabled());
        assertEquals(model.getDisplay().getKeyboardLayout(), transform.getDisplay().getKeyboardLayout());
        assertEquals(model.isDeleteProtected(), transform.isDeleteProtected());
        assertEquals(model.isTunnelMigration(), transform.isTunnelMigration());
        assertEquals(model.getMigrationDowntime(), transform.getMigrationDowntime());
        assertEquals(model.getSerialNumber().getPolicy(), transform.getSerialNumber().getPolicy());
        assertEquals(model.getSerialNumber().getValue(), transform.getSerialNumber().getValue());
        assertEquals(model.getDisplay().isFileTransferEnabled(), transform.getDisplay().isFileTransferEnabled());
        assertEquals(model.getDisplay().isCopyPasteEnabled(), transform.getDisplay().isCopyPasteEnabled());
        assertEquals(model.isStartPaused(), transform.isStartPaused());
        assertEquals(model.getMigration().getAutoConverge(), transform.getMigration().getAutoConverge());
        assertEquals(model.getMigration().getCompressed(), transform.getMigration().getCompressed());
        assertEquals(model.getDisplay().getDisconnectAction(), transform.getDisplay().getDisconnectAction());
    }

    private void assertHostsListMatch(List<Host> modelHostsList, List<Host> trnsfHostsList) {
        for (Host host : modelHostsList){
            boolean foundInTransformation = false;
            for (Host otherHost : trnsfHostsList){
                if (host.getId().equals(otherHost.getId())){
                    foundInTransformation = true;
                    break;
                }
            }
            assertTrue("Umatching dedicated host in Placement Policy", foundInTransformation);
        }
    }

    @Test
    public void testVmPayloadMapToPaylod() {
        VmPayload vmPayload = new VmPayload();
        vmPayload.setDeviceType(org.ovirt.engine.core.common.utils.VmDeviceType.CDROM);
        vmPayload.setVolumeId("CD-VOL");
        Payload payload = VmMapper.map(vmPayload, null);
        assertEquals(vmPayload.getDeviceType().name(), payload.getType().name());
        assertEquals(vmPayload.getVolumeId(), payload.getVolumeId());
    }

    @Test
    public void testPayloadMapToVmPaylod() {
        Payload payload = new Payload();
        payload.setType(VmDeviceType.CDROM);
        payload.setVolumeId("CD-VOL");
        VmPayload vmPayload = VmMapper.map(payload, null);
        assertEquals(payload.getType().name(), vmPayload.getDeviceType().name());
        assertEquals(payload.getVolumeId(), vmPayload.getVolumeId());
    }

    @Test
    public void testUpdateHostPinningPolicy() {
        final VmStatic vmTemplate = new VmStatic();
        vmTemplate.setDedicatedVmForVdsList(Guid.newGuid());
        final Vm vm = new Vm();
        vm.setPlacementPolicy(createPlacementPolicy(Guid.newGuid(), Guid.newGuid()));
        final VmStatic mappedVm = VmMapper.map(vm, vmTemplate);

        final List<Guid> hosts = new ArrayList<>();
        for (Host host : vm.getPlacementPolicy().getHosts().getHosts()){
            hosts.add(Guid.createGuidFromString(host.getId()));
        }
        assertEquals(new HashSet(hosts), new HashSet(mappedVm.getDedicatedVmForVdsList()));
    }


    @Test
    public void ovfConfigurationMap() {
        String ovfConfig = "config";
        ConfigurationType configurationType = ConfigurationType.OVF;
        Vm model = new Vm();
        VmMapper.map(ovfConfig, ConfigurationType.OVF, model);
        assertNotNull(model.getInitialization());
        assertNotNull(model.getInitialization().getConfiguration());
        assertEquals(model.getInitialization().getConfiguration().getData(), ovfConfig);
        assertEquals(model.getInitialization().getConfiguration().getType(),
                configurationType);
    }

    @Test
    public void testGuestFQDN() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);
        vmDynamic.setVmFQDN("localhost.localdomain");
        vm.setDynamicData(vmDynamic);
        Vm map = VmMapper.map(vm, (Vm) null);
        assertNotNull(map.getFqdn());
        assertEquals(map.getFqdn(), "localhost.localdomain");
    }

    @Test
    public void testGuestTimezone() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);
        vmDynamic.setGuestOsTimezoneName("This is not a timezone");
        vmDynamic.setGuestOsTimezoneOffset(-60);
        vm.setDynamicData(vmDynamic);
        Vm map = VmMapper.map(vm, (Vm) null);
        assertNotNull(map.getGuestTimeZone());
        assertEquals(map.getGuestTimeZone().getUtcOffset(), "-01:00");
        assertEquals(map.getGuestTimeZone().getName(), "This is not a timezone");
    }

    @Test
    public void testGuestOs() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);

        vmDynamic.setGuestOsType(OsType.Linux);
        vmDynamic.setGuestOsCodename("Santiago");
        vmDynamic.setGuestOsDistribution("Red Hat Enterprise Linux Server");
        vmDynamic.setGuestOsVersion("6.5");
        vmDynamic.setGuestOsArch(ArchitectureType.x86_64);
        vmDynamic.setGuestOsKernelVersion("2.6.32-431.el6.x86_64");

        vm.setDynamicData(vmDynamic);
        Vm map = VmMapper.map(vm, (Vm) null);

        assertNotNull(map.getGuestOperatingSystem());
        assertEquals(map.getGuestOperatingSystem().getFamily(), "Linux");
        assertEquals(map.getGuestOperatingSystem().getCodename(), "Santiago");
        assertEquals(map.getGuestOperatingSystem().getDistribution(), "Red Hat Enterprise Linux Server");
        assertEquals(map.getGuestOperatingSystem().getVersion().getFullVersion(), "6.5");
        assertNotNull(map.getGuestOperatingSystem().getVersion().getMajor());
        assertEquals((long) map.getGuestOperatingSystem().getVersion().getMajor(), 6);
        assertNotNull(map.getGuestOperatingSystem().getVersion().getMinor());
        assertEquals((long) map.getGuestOperatingSystem().getVersion().getMinor(), 5);
        assertNull(map.getGuestOperatingSystem().getVersion().getBuild());
        assertNull(map.getGuestOperatingSystem().getVersion().getRevision());
        assertEquals(map.getGuestOperatingSystem().getArchitecture(), "x86_64");
        assertEquals(map.getGuestOperatingSystem().getKernel().getVersion().getFullVersion(), "2.6.32-431.el6.x86_64");
        assertEquals((long)map.getGuestOperatingSystem().getKernel().getVersion().getMajor(), 2);
        assertEquals((long)map.getGuestOperatingSystem().getKernel().getVersion().getMinor(), 6);
        assertEquals((long)map.getGuestOperatingSystem().getKernel().getVersion().getBuild(), 32);
        assertEquals((long)map.getGuestOperatingSystem().getKernel().getVersion().getRevision(), 431);
    }

    @Test
    public void testGuestOs2() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setStatus(VMStatus.Up);

        vmDynamic.setGuestOsType(OsType.Windows);
        vmDynamic.setGuestOsCodename("");
        vmDynamic.setGuestOsDistribution("");
        vmDynamic.setGuestOsVersion("6.2.4800");
        vmDynamic.setGuestOsArch(ArchitectureType.x86_64);
        vmDynamic.setGuestOsKernelVersion("");

        vm.setDynamicData(vmDynamic);
        Vm map = VmMapper.map(vm, (Vm) null);

        assertNotNull(map.getGuestOperatingSystem());
        assertEquals(map.getGuestOperatingSystem().getFamily(), "Windows");
        assertEquals(map.getGuestOperatingSystem().getCodename(), "");
        assertEquals(map.getGuestOperatingSystem().getDistribution(), "");
        assertEquals(map.getGuestOperatingSystem().getVersion().getFullVersion(), "6.2.4800");
        assertNotNull(map.getGuestOperatingSystem().getVersion().getMajor());
        assertEquals((long) map.getGuestOperatingSystem().getVersion().getMajor(), 6);
        assertNotNull(map.getGuestOperatingSystem().getVersion().getMinor());
        assertEquals((long) map.getGuestOperatingSystem().getVersion().getMinor(), 2);
        assertNotNull(map.getGuestOperatingSystem().getVersion().getBuild());
        assertEquals((long) map.getGuestOperatingSystem().getVersion().getBuild(), 4800);
        assertNull(map.getGuestOperatingSystem().getVersion().getRevision());
        assertEquals(map.getGuestOperatingSystem().getArchitecture(), "x86_64");
        assertNull(map.getGuestOperatingSystem().getKernel());
    }

    @Test
    public void testDisplayPort() {
        org.ovirt.engine.core.common.businessentities.VM entity =
                new org.ovirt.engine.core.common.businessentities.VM();
        entity.setStatus(VMStatus.Up);
        entity.getGraphicsInfos().put(GraphicsType.SPICE, new GraphicsInfo());
        entity.getGraphicsInfos().get(GraphicsType.SPICE)
                .setPort(5900)
                .setTlsPort(9999);
        Vm model = VmMapper.map(entity, (Vm) null);
        entity.getGraphicsInfos().put(GraphicsType.SPICE, new GraphicsInfo());
        entity.getGraphicsInfos().get(GraphicsType.SPICE)
                .setPort(null)
                .setTlsPort(null);
        model = VmMapper.map(entity, (Vm) null);
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
        Vm model = VmMapper.map(entity, (Vm) null);
        assertEquals(GLOBAL_SPICE_PROXY, model.getDisplay().getProxy());
    }

    @Test
    public void testClusterSpiceProxy() {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        entity.setClusterSpiceProxy(CLUSTER_SPICE_PROXY);
        Vm model = VmMapper.map(entity, (Vm) null);
        assertEquals(CLUSTER_SPICE_PROXY, model.getDisplay().getProxy());
    }

    @Test
    public void testPoolSpiceProxy() {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        entity.setVmPoolSpiceProxy(POOL_SPICE_PROXY);
        Vm model = VmMapper.map(entity, (Vm) null);
        assertEquals(POOL_SPICE_PROXY, model.getDisplay().getProxy());
    }

    @Test
    public void testMapHostId() {
        org.ovirt.engine.core.common.businessentities.VM entity =
                new org.ovirt.engine.core.common.businessentities.VM();
        entity.setStatus(VMStatus.Up);
        Guid guid = Guid.newGuid();
        entity.setRunOnVds(guid);
        Vm model = VmMapper.map(entity, (Vm) null);
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
        assertNotNull(cpuTune.getVcpuPins());
        assertEquals(1, cpuTune.getVcpuPins().getVcpuPins().size());
        assertEquals(0, (int) cpuTune.getVcpuPins().getVcpuPins().get(0).getVcpu());
        assertEquals("0", cpuTune.getVcpuPins().getVcpuPins().get(0).getCpuSet());
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
        VcpuPin pin = VmMapper.stringToVCpupin("1#1-4,6");
        assertEquals(1, (int) pin.getVcpu());
        assertEquals("1-4,6", pin.getCpuSet());
    }

    @Test()
    public void stringToVCpupinSimple() {
        VcpuPin pin = VmMapper.stringToVCpupin("1#1");
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
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyIsSetDisabled() {
        Usb usb = new Usb();
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicySetDisabled() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyUsbTypeNative() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType(UsbType.NATIVE);
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyUsbTypeLegacy() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnCreate(usb), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotNullUsb() {
        Usb usb = null;
        UsbPolicy currentPolicy = UsbPolicy.DISABLED;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotNullUsb() {
        Usb usb = null;
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_LEGACY;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotNullUsb() {
        Usb usb = null;
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_NATIVE;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNotSetPolicyNotSetUsb() {
        Usb usb = new Usb();
        UsbPolicy currentPolicy = UsbPolicy.DISABLED;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotEnabledNotSetPolicyNotSetUsb() {
        Usb usb = new Usb();
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_LEGACY;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNotSetPolicyNotSetUsb() {
        Usb usb = new Usb();
        UsbPolicy currentPolicy = UsbPolicy.ENABLED_NATIVE;
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, currentPolicy), currentPolicy);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNotSetLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNotSetNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setType(UsbType.NATIVE);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotEnabledNotSetLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_LEGACY), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNotSetLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotEnabledNotSetNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setType(UsbType.NATIVE);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_LEGACY), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNotSetNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setType(UsbType.NATIVE);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotDisabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotDisabledNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType(UsbType.NATIVE);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyLegacyGotDisabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_LEGACY), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotDisabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledLegacyPolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType(UsbType.LEGACY);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyNativeGotEnabledNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType(UsbType.NATIVE);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.ENABLED_NATIVE), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyOnUpdateCurrentlyDisabledGotEnabledNativePolicyUsb() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType(UsbType.NATIVE);
        assertEquals(VmMapper.getUsbPolicyOnUpdate(usb, UsbPolicy.DISABLED), UsbPolicy.ENABLED_NATIVE);
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
