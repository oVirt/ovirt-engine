package org.ovirt.engine.ui.userportal.widget;

import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import com.google.gwt.safehtml.shared.SafeHtml;

public class QuotaCPUProgressBar extends QuotaProgressBar {

    public QuotaCPUProgressBar(QuotaUsagePerUser quotaUsagePerUser) {
        super(quotaUsagePerUser);
    }

    public QuotaCPUProgressBar() {
        super();
    }

    @Override
    protected void setValuesByType(QuotaUsagePerUser quotaUsagePerUser) {
        setValues(quotaUsagePerUser.getVcpuLimit(),
                quotaUsagePerUser.getVcpuTotalUsage() - quotaUsagePerUser.getVcpuUsageForUser(),
                quotaUsagePerUser.getVcpuUsageForUser());
    }

    @Override
    protected SafeHtml getTooltip() {
        if (quotaUsagePerUser.getVcpuLimit() == UNLIMITED) {
            return null;
        }
        return templateWithLabels(String.valueOf(quotaUsagePerUser.getVcpuLimit()),
                (int) (quotaUsagePerUser.getOthersVcpuUsagePercentage() + quotaUsagePerUser.getUserVcpuUsagePercentage()),
                String.valueOf(quotaUsagePerUser.getVcpuTotalUsage()),
                (int) quotaUsagePerUser.getUserVcpuUsagePercentage(),
                String.valueOf(quotaUsagePerUser.getVcpuUsageForUser()),
                (int) quotaUsagePerUser.getOthersVcpuUsagePercentage(),
                String.valueOf(quotaUsagePerUser.getVcpuTotalUsage() - quotaUsagePerUser.getVcpuUsageForUser()),
                (int) Math.max(100 - (quotaUsagePerUser.getOthersVcpuUsagePercentage() + quotaUsagePerUser.getUserVcpuUsagePercentage()),
                        0),
                String.valueOf(quotaUsagePerUser.getFreeVcpu()));
    }
}
