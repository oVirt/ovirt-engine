package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

/**
 * It allows for frontend to distinguish between regular template and latest template.
 * <p>
 *     Note: Latest template instance equals to its pattern.
 *     <pre>
 *         VmTemplate a = ...
 *         LatestVmTemplate b = new LatestVmTemplate(a);
 *         a.equals(b); // returns true
 *     </pre>
 * </p>
 */
public class LatestVmTemplate extends VmTemplate {

    public LatestVmTemplate(VmTemplate template) {
        super(template);

        setNaming();
    }

    /**
     * Just because serializable classes require non-parametric constructor.
     */
    public LatestVmTemplate() {
        super();
        setNaming();
    }

    private void setNaming() {
        final UIConstants constants = ConstantsManager.getInstance().getConstants();
        this.setTemplateVersionName(constants.latestTemplateVersionName());
        this.setDescription(constants.latestTemplateVersionDescription());
    }
}
