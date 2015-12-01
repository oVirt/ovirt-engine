package org.ovirt.engine.ui.userportal.widget;

import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;

public class QuotaStorageProgressBar extends QuotaProgressBar {

    public QuotaStorageProgressBar(QuotaUsagePerUser quotaUsagePerUser) {
        super(quotaUsagePerUser);
    }

    public QuotaStorageProgressBar() {
        super();
    }

    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<>(SizeConverter.SizeUnit.GiB);

    @Override
    protected void setValuesByType(QuotaUsagePerUser quotaUsagePerUser) {
        setValues(quotaUsagePerUser.getStorageLimit(),
                quotaUsagePerUser.getStorageTotalUsage() - quotaUsagePerUser.getStorageUsageForUser(),
                quotaUsagePerUser.getStorageUsageForUser());
    }

    @Override
    protected SafeHtml getTooltip() {
        if (quotaUsagePerUser.getStorageLimit() == UNLIMITED) {
            return null;
        }
        return templateWithLabels(renderStorage(quotaUsagePerUser.getStorageLimit()),
                (int) (quotaUsagePerUser.getOthersStorageUsagePercentage() + quotaUsagePerUser.getUserStorageUsagePercentage()),
                renderStorage(quotaUsagePerUser.getStorageTotalUsage()),
                (int) quotaUsagePerUser.getUserStorageUsagePercentage(),
                renderStorage(quotaUsagePerUser.getStorageUsageForUser()),
                (int) quotaUsagePerUser.getOthersStorageUsagePercentage(),
                renderStorage(quotaUsagePerUser.getStorageTotalUsage() - quotaUsagePerUser.getStorageUsageForUser()),
                (int) Math.max(100 - (quotaUsagePerUser.getOthersStorageUsagePercentage() + quotaUsagePerUser.getUserStorageUsagePercentage()),
                        0),
                renderStorage(quotaUsagePerUser.getFreeStorage()));
    }

    private String renderStorage(double storage) {
        return storage <= 0 ? "0" : diskSizeRenderer.render(storage); //$NON-NLS-1$
    }
}
