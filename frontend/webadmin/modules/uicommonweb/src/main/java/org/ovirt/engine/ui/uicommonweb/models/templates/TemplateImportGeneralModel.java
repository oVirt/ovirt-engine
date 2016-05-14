package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;

public class TemplateImportGeneralModel extends TemplateGeneralModel {
    @Override
    public VmTemplate getEntity() {
        return ((ImportTemplateData) (Object) super.getEntity()).getTemplate();
    }
}
