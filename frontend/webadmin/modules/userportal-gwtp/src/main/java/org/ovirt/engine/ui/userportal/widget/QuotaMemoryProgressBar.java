package org.ovirt.engine.ui.userportal.widget;

import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import com.google.gwt.safehtml.shared.SafeHtml;

public class QuotaMemoryProgressBar extends QuotaProgressBar {

    private static final double GIGA = 1024;
    private static final double MB_GB_THRESHOLD = 4; // over this threshold number would be presented in GB not MB
    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<>(SizeConverter.SizeUnit.GiB);

    public QuotaMemoryProgressBar(QuotaUsagePerUser quotaUsagePerUser) {
        super(quotaUsagePerUser);
    }

    public QuotaMemoryProgressBar() {
        super();
    }

    @Override
    protected void setValuesByType(QuotaUsagePerUser quotaUsagePerUser) {
        setValues(quotaUsagePerUser.getMemoryLimit(),
                quotaUsagePerUser.getMemoryTotalUsage() - quotaUsagePerUser.getMemoryUsageForUser(),
                quotaUsagePerUser.getMemoryUsageForUser());
    }

    @Override
    protected SafeHtml getTooltip() {
        if (quotaUsagePerUser.getMemoryLimit() == UNLIMITED) {
            return null;
        }
        return templateWithLabels(renderMemory(quotaUsagePerUser.getMemoryLimit()),
                (int) (quotaUsagePerUser.getOthersMemoryUsagePercentage() + quotaUsagePerUser.getUserMemoryUsagePercentage()),
                renderMemory(quotaUsagePerUser.getMemoryTotalUsage()),
                (int) quotaUsagePerUser.getUserMemoryUsagePercentage(),
                renderMemory(quotaUsagePerUser.getMemoryUsageForUser()),
                (int) quotaUsagePerUser.getOthersMemoryUsagePercentage(),
                renderMemory(quotaUsagePerUser.getMemoryTotalUsage() - quotaUsagePerUser.getMemoryUsageForUser()),
                (int) Math.max(100 - (quotaUsagePerUser.getOthersMemoryUsagePercentage() + quotaUsagePerUser.getUserMemoryUsagePercentage()),
                        0),
                renderMemory(quotaUsagePerUser.getFreeMemory()));
    }

    private String renderMemory(double memory) {
        if (memory <= 0) {
            return "0"; //$NON-NLS-1$
        }
        return memory > Math.abs(MB_GB_THRESHOLD * GIGA) ? diskSizeRenderer.render(memory / GIGA) : (int) memory + "MB"; //$NON-NLS-1$
    }
}
