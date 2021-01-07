package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.CpuTune;
import org.ovirt.engine.api.model.DisplayDisconnectAction;
import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.NicConfiguration;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.TimeZone;
import org.ovirt.engine.api.model.VcpuPin;
import org.ovirt.engine.api.model.VcpuPins;
import org.ovirt.engine.api.model.VmStorageErrorResumeBehaviour;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class TemplateMapperTest
        extends AbstractInvertibleMappingTest<Template, VmTemplate, VmTemplate> {

    public TemplateMapperTest() {
        super(Template.class, VmTemplate.class, VmTemplate.class);
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        OsTypeMockUtils.mockOsTypes();
    }

    @Override
    protected Template postPopulate(Template from) {
        from.setType(VmType.DESKTOP);
        from.setStorageErrorResumeBehaviour(VmStorageErrorResumeBehaviour.AUTO_RESUME);
        from.setOrigin(OriginType.VMWARE.name().toLowerCase());
        List<BootDevice> devices = from.getOs().getBoot().getDevices().getDevices();
        for (int i = 0; i < devices.size(); i++) {
            devices.set(i, BootDevice.NETWORK);
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
        from.getSerialNumber().setPolicy(SerialNumberPolicy.CUSTOM);
        from.getMigration().setAutoConverge(InheritableBoolean.TRUE);
        from.getMigration().setCompressed(InheritableBoolean.TRUE);
        from.getDisplay().setDisconnectAction(DisplayDisconnectAction.LOCK_SCREEN.toString());
        for (NicConfiguration nicConfiguration : from.getInitialization().getNicConfigurations().getNicConfigurations()) {
            nicConfiguration.setBootProtocol(BootProtocol.STATIC);
        }
        return from;
    }

    @Override
    protected void verify(Template model, Template transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getComment(), transform.getComment());
        assertEquals(model.getType(), transform.getType());
        assertEquals(model.getOrigin(), transform.getOrigin());
        assertTrue(Math.abs(model.getMemory() - transform.getMemory()) <= (1024 * 1024));
        assertNotNull(transform.getCluster());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertNotNull(transform.getCpu());
        assertNotNull(transform.getCpu().getTopology());
        assertTrue(Math.abs(model.getCpu().getTopology().getCores() -
                transform.getCpu().getTopology().getCores()) < model.getCpu().getTopology().getSockets());
        assertEquals(model.getCpu().getTopology().getSockets(),
                transform.getCpu().getTopology().getSockets());
        assertNotNull(transform.isSetOs());
        assertEquals(model.getBios().getBootMenu().isEnabled(), transform.getBios().getBootMenu().isEnabled());
        assertTrue(transform.getOs().isSetBoot());
        assertEquals(model.getOs().getBoot().getDevices().getDevices(),
                transform.getOs().getBoot().getDevices().getDevices());
        assertEquals(model.getOs().getKernel(), transform.getOs().getKernel());
        assertEquals(model.getOs().getInitrd(), transform.getOs().getInitrd());
        assertEquals(model.getOs().getCmdline(), transform.getOs().getCmdline());
        assertNotNull(model.getDisplay());
        assertEquals(model.getDisplay().getMonitors(), transform.getDisplay().getMonitors());
        assertEquals(model.getDisplay().isAllowOverride(), transform.getDisplay().isAllowOverride());
        assertEquals(model.getDisplay().getKeyboardLayout(), transform.getDisplay().getKeyboardLayout());
        assertEquals(model.getTimeZone().getName(), transform.getTimeZone().getName());
        assertEquals(model.getDisplay().isSmartcardEnabled(), transform.getDisplay().isSmartcardEnabled());
        assertEquals(model.isDeleteProtected(), transform.isDeleteProtected());
        assertEquals(model.isTunnelMigration(), transform.isTunnelMigration());
        assertEquals(model.getMigrationDowntime(), transform.getMigrationDowntime());
        assertEquals(model.getVersion().getVersionName(), transform.getVersion().getVersionName());
        assertEquals(model.getVersion().getBaseTemplate().getId(), transform.getVersion().getBaseTemplate().getId());
        assertEquals(model.getSerialNumber().getPolicy(), transform.getSerialNumber().getPolicy());
        assertEquals(model.getSerialNumber().getValue(), transform.getSerialNumber().getValue());
        assertEquals(model.getDisplay().isFileTransferEnabled(), transform.getDisplay().isFileTransferEnabled());
        assertEquals(model.getDisplay().isCopyPasteEnabled(), transform.getDisplay().isCopyPasteEnabled());
        assertEquals(model.isStartPaused(), transform.isStartPaused());
        assertEquals(model.getMigration().getAutoConverge(), transform.getMigration().getAutoConverge());
        assertEquals(model.getMigration().getCompressed(), transform.getMigration().getCompressed());
        assertEquals(model.getDisplay().getDisconnectAction(), transform.getDisplay().getDisconnectAction());
    }
}
