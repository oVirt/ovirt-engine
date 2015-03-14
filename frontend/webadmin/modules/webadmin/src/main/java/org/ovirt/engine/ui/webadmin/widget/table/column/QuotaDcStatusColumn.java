package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code QuotaDcStatusColumn}.
 */
public class QuotaDcStatusColumn extends AbstractImageResourceColumn<Quota> {

    private final static ApplicationResources resources = AssetProvider.getResources();

    @Override
    public ImageResource getValue(Quota quota) {
        if (quota.getQuotaEnforcementType() == null) {
            return resources.iconDisable();
        }

        setEnumTitle(quota.getQuotaEnforcementType());
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

}
