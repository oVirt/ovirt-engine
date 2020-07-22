package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class TemplateAdditionalStatusColumn extends EntityAdditionalStatusColumn<VmTemplate> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public SafeHtml getEntityValue(VmTemplate template) {
        if (!template.isManaged()) {
            return getImageSafeHtml(resources.container());
        }
        return null;
    }

    @Override
    public SafeHtml getEntityTooltip(VmTemplate template) {
        if (!template.isManaged()) {
            return SafeHtmlUtils.fromTrustedString(constants.providedByContainerPlatform());
        }
        return null;
    }

    @Override
    protected VmTemplate getEntityObject(VmTemplate template) {
        return template;
    }
}
