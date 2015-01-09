package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;

public class QuotaUtilizationStatusColumn<IVdcQueryable> extends AbstractWebAdminImageResourceColumn<IVdcQueryable> {

    private static final ApplicationConstants applicationConstants = ClientGinjectorProvider.getApplicationConstants();

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

        if (quotaExceeded) {
            setTitle(applicationConstants.quotaExceeded());
        }

        return quotaExceeded ? getResources().alertImage() : null;
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

    private ApplicationResources getResources() {
        // Get a reference to the application resources:
        return ClientGinjectorProvider.getApplicationResources();
    }
}
