package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VmTemplate;


public class ImportTemplateData extends ImportEntityData {

    public ImportTemplateData(VmTemplate template) {
        setEntity(template);
    }

    public VmTemplate getTemplate() {
        return (VmTemplate) getEntity();
    }
}
