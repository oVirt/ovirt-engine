package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Quota;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code QuotaDcStatusColumn}.
 */
public class QuotaDcStatusColumn extends AbstractWebAdminImageResourceColumn<Quota> {

    @Override
    public ImageResource getValue(Quota quota) {
        if (quota.getQuotaEnforcementType() == null) {
            return getApplicationResources().iconDisable();
        }

        setEnumTitle(quota.getQuotaEnforcementType());
        switch (quota.getQuotaEnforcementType()) {
        case HARD_ENFORCEMENT:
            return getApplicationResources().iconEnforce();
        case SOFT_ENFORCEMENT:
            return getApplicationResources().iconAudit();
        case DISABLED:
            return getApplicationResources().iconDisable();
        default:
            return getApplicationResources().iconDisable();
        }
    }

}
