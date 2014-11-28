package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Boot;
import org.ovirt.engine.api.model.BootDevice;
import org.ovirt.engine.api.model.InheritableBoolean;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.VmType;
import org.ovirt.engine.api.restapi.utils.OsTypeMockUtils;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class TemplateMapperTest
        extends AbstractInvertibleMappingTest<Template, VmTemplate, VmTemplate> {

    public TemplateMapperTest() {
        super(Template.class, VmTemplate.class, VmTemplate.class);
    }

    @Override
    public void setUp() {
        super.setUp();
        OsTypeMockUtils.mockOsTypes();
    }

    @Override
    protected Template postPopulate(Template from) {
        from.setType(MappingTestHelper.shuffle(VmType.class).value());
        from.setOrigin(OriginType.VMWARE.name().toLowerCase());
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
        from.getSerialNumber().setPolicy(SerialNumberPolicy.CUSTOM.value());
        from.getMigration().setAutoConverge(InheritableBoolean.TRUE.value());
        from.getMigration().setCompressed(InheritableBoolean.TRUE.value());
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
        assertEquals(model.getOs().getBoot().size(), transform.getOs().getBoot().size());
        for (int i = 0; i < model.getOs().getBoot().size(); i++) {
            assertEquals(model.getOs().getBoot().get(i).getDev(),
                    transform.getOs().getBoot().get(i).getDev());
        }
        assertEquals(model.getOs().getKernel(), transform.getOs().getKernel());
        assertEquals(model.getOs().getInitrd(), transform.getOs().getInitrd());
        assertEquals(model.getOs().getCmdline(), transform.getOs().getCmdline());
        assertNotNull(model.getDisplay());
        assertEquals(model.getDisplay().getMonitors(), transform.getDisplay().getMonitors());
        assertEquals(model.getDisplay().isSingleQxlPci(), transform.getDisplay().isSingleQxlPci());
        assertEquals(model.getDisplay().isAllowOverride(), transform.getDisplay().isAllowOverride());
        assertEquals(model.getDisplay().getKeyboardLayout(), transform.getDisplay().getKeyboardLayout());
        assertEquals(model.getTimezone(), transform.getTimezone());
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
    }
}
