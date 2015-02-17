package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;


public class ImportTemplateData extends ImportEntityData<VmTemplate> {

    public ImportTemplateData(VmTemplate template) {
        setEntity(template);
    }

    public VmTemplate getTemplate() {
        return getEntity();
    }

    @Override
    public ArchitectureType getArchType() {
        return getEntity().getClusterArch();
    }

    @Override
    public String getName() {
        return getEntity().getName();
    }
}
