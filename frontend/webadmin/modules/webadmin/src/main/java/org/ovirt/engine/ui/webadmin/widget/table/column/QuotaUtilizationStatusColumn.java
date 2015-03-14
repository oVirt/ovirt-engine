package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class QuotaUtilizationStatusColumn<IVdcQueryable> extends AbstractImageResourceColumn<IVdcQueryable> {

    private final static ApplicationResources resources = AssetProvider.getResources();
    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageResource getValue(IVdcQueryable quota) {
        boolean quotaExceeded = false;
        if (quota instanceof QuotaStorage) {
            quotaExceeded = getQuotaExceeded((QuotaStorage)quota);
        } else if (quota instanceof QuotaVdsGroup) {
            quotaExceeded = getQuotaExceeded((QuotaVdsGroup)quota);
        } else {
            return null;
        }

        return quotaExceeded ? resources.alertImage() : null;
    }

    @Override
    public SafeHtml getTooltip(IVdcQueryable quota) {
        boolean quotaExceeded = false;
        if (quota instanceof QuotaStorage) {
            quotaExceeded = getQuotaExceeded((QuotaStorage)quota);
        } else if (quota instanceof QuotaVdsGroup) {
            quotaExceeded = getQuotaExceeded((QuotaVdsGroup)quota);
        }

        if (quotaExceeded) {
            return SafeHtmlUtils.fromSafeConstant(constants.quotaExceeded());
        }
        return null;

    }

    private boolean getQuotaExceeded(QuotaStorage quota) {
        if (quota.getStorageSizeGB() == null) {
            return false;
        }
        return quota.getStorageSizeGB().longValue() != QuotaStorage.UNLIMITED.longValue()
                && quota.getStorageSizeGB() < quota.getStorageSizeGBUsage();
    }

    private boolean getQuotaExceeded(QuotaVdsGroup quota) {
        return (quota.getMemSizeMB() != null && !quota.getMemSizeMB().equals(QuotaVdsGroup.UNLIMITED_MEM)
                && quota.getMemSizeMB() < quota.getMemSizeMBUsage())
                || (quota.getVirtualCpu() != null && !quota.getVirtualCpu().equals(QuotaVdsGroup.UNLIMITED_VCPU)
                && quota.getVirtualCpu() < quota.getVirtualCpuUsage());
    }

}
