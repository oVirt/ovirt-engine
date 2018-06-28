package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;


public class ImportTemplateData extends ImportEntityData<VmTemplate> {

    private boolean nameExistsInTheSystem;
    private String templateName;

    public ImportTemplateData(VmTemplate template) {
        setEntity(template);
        templateName = template.getName();
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
        return templateName;
    }

    public boolean isNameExistsInTheSystem() {
        return nameExistsInTheSystem;
    }

    public void setNameExistsInTheSystem(boolean nameExistsInTheSystem) {
        this.nameExistsInTheSystem = nameExistsInTheSystem;
    }
}
