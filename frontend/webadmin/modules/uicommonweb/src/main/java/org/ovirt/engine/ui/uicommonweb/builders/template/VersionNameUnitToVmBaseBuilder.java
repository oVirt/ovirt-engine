package org.ovirt.engine.ui.uicommonweb.builders.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class VersionNameUnitToVmBaseBuilder extends BaseSyncBuilder<UnitVmModel, VmTemplate> {
    @Override
    protected void build(UnitVmModel source, VmTemplate destination) {
        destination.setTemplateVersionName(source.getTemplateVersionName().getEntity());
    }
}
