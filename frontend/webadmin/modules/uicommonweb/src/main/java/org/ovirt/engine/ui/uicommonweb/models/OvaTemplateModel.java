package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.VmTemplate;

/**
 * Combines together ova file name and the template read from that ova file
 *
 */
public class OvaTemplateModel extends EntityModel<VmTemplate> {

    private String ovaFileName;

    public OvaTemplateModel() {
        super();
    }

    public OvaTemplateModel(String ovaFileName, VmTemplate template) {
        super(template);
        this.ovaFileName = ovaFileName;
    }

    public void setOvaFileName(String ovaFileName) {
        this.ovaFileName = ovaFileName;
    }

    public String getOvaFileName() {
        return ovaFileName;
    }
}
