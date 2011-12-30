package org.ovirt.engine.api.restapi.types;

import org.junit.Test;

import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.DisplayType;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

import static org.ovirt.engine.api.restapi.types.MappingTestHelper.rand;

import static org.easymock.classextension.EasyMock.expect;

public class VmMapperTest extends
        AbstractInvertibleMappingTest<VM, VmStatic, org.ovirt.engine.core.common.businessentities.VM> {

    public VmMapperTest() {
        super(VM.class, VmStatic.class, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    protected void setUpConfigExpectations() {
        expect(Config.<Integer> GetValue(ConfigValues.NumberVmRefreshesBeforeSave)).andReturn(
                rand(10)).anyTimes();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.VM getInverse(VmStatic to) {
        VmStatistics statistics = new VmStatistics();
        statistics.setcpu_user(new Double(10L));
        statistics.setcpu_sys(new Double(20L));
        VmDynamic dynamic = new VmDynamic();
        dynamic.setdisplay_type(to.getdefault_display_type());
        org.ovirt.engine.core.common.businessentities.VM ret =
            new org.ovirt.engine.core.common.businessentities.VM(to,
                                                            dynamic,
                                                            statistics);
        ret.setusage_mem_percent(Integer.valueOf(50));
        return ret;
    }

    @Override
    protected VM postPopulate(VM from) {
        from.setType(MappingTestHelper.shuffle(VmType.class).value());
        from.setOrigin(OriginType.VMWARE.name().toLowerCase());
        from.getDisplay().setType(MappingTestHelper.shuffle(DisplayType.class).value());
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
                    transform.getCpu().getTopology().getCores()) <
               model.getCpu().getTopology().getSockets());
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
        assertEquals(model.getDisplay().isAllowReconnect(), transform.getDisplay().isAllowReconnect());
        assertEquals(model.getPlacementPolicy().getHost().getId(), transform.getPlacementPolicy().getHost().getId());
        assertTrue(Math.abs(model.getMemoryPolicy().getGuaranteed() - transform.getMemoryPolicy().getGuaranteed()) <= (1024 * 1024));
        assertEquals(model.getDomain().getName(), transform.getDomain().getName());
        assertEquals(model.getTimezone(), transform.getTimezone());
        assertEquals(model.getUsb().isEnabled(), transform.getUsb().isEnabled());
    }

    @Test
    public void testGustIp() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setvm_ip("2.2.2.2");
        vm.setDynamicData(vmDynamic);

        VM map = VmMapper.map(vm, null);
        assertNotNull(map.getGuestInfo().getIps().getIPs().get(0));
        assertEquals(map.getGuestInfo().getIps().getIPs().get(0).getAddress(), "2.2.2.2");
    }

    @Test
    public void testGustIps() {
        org.ovirt.engine.core.common.businessentities.VM vm = new org.ovirt.engine.core.common.businessentities.VM();
        VmDynamic vmDynamic = new VmDynamic();
        vmDynamic.setvm_ip("2.2.2.2 2.2.2.3 2.2.2.4");
        vm.setDynamicData(vmDynamic);

        VM map = VmMapper.map(vm, null);
        assertNotNull(map.getGuestInfo().getIps().getIPs().get(0));
        assertEquals(map.getGuestInfo().getIps().getIPs().get(0).getAddress(), "2.2.2.2");
        assertEquals(map.getGuestInfo().getIps().getIPs().get(1).getAddress(), "2.2.2.3");
        assertEquals(map.getGuestInfo().getIps().getIPs().get(2).getAddress(), "2.2.2.4");
    }

    @Test
    public void testDisplayPort() {
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        entity.setdisplay(5900);
        entity.setdisplay_secure_port(9999);
        VM model = VmMapper.map(entity, null);
        assertTrue(model.getDisplay().getPort()==5900);
        assertTrue(model.getDisplay().getSecurePort()==9999);
        entity.setdisplay(-1);
        entity.setdisplay_secure_port(-1);
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
        org.ovirt.engine.core.common.businessentities.VM entity = new org.ovirt.engine.core.common.businessentities.VM();
        Guid guid = NGuid.NewGuid();
        entity.setrun_on_vds(guid);
        VM model = VmMapper.map(entity, null);
        assertEquals(guid.toString(), model.getHost().getId());
    }
}
