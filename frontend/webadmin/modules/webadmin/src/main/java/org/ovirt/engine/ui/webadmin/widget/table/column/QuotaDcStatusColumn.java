package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Image column that corresponds to XAML {@code QuotaDcStatusColumn}.
 */
public class QuotaDcStatusColumn extends AbstractImageResourceColumn<Quota> {

    private static final ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(Quota quota) {
        if (quota.getQuotaEnforcementType() == null) {
            return resources.iconDisable();
        }

        switch (quota.getQuotaEnforcementType()) {
        case HARD_ENFORCEMENT:
            return resources.iconEnforce();
        case SOFT_ENFORCEMENT:
            return resources.iconAudit();
        case DISABLED:
            return resources.iconDisable();
        default:
            return resources.iconDisable();
        }
    }

    @Override
    public SafeHtml getTooltip(Quota quota) {
        String tooltipContent = EnumTranslator.getInstance().translate(quota.getQuotaEnforcementType());
        return SafeHtmlUtils.fromString(tooltipContent);
    }

}
