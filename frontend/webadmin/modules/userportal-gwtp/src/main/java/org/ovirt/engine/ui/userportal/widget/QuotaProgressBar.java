package org.ovirt.engine.ui.userportal.widget;

import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.gwt.safehtml.shared.SafeHtml;

public abstract class QuotaProgressBar extends DoublePercentageProgressBar {

    public static final int UNLIMITED = -1;

    protected QuotaUsagePerUser quotaUsagePerUser;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public QuotaProgressBar(QuotaUsagePerUser quotaUsagePerUser) {
        setQuotaUsagePerUser(quotaUsagePerUser);
        tooltip = new WidgetTooltip(this);
    }

    public QuotaProgressBar() {
        tooltip = new WidgetTooltip(this);
    }

    public void setQuotaUsagePerUser(QuotaUsagePerUser quotaUsagePerUser) {
        this.quotaUsagePerUser = quotaUsagePerUser;
        setValuesByType(quotaUsagePerUser);
    }

    protected abstract void setValuesByType(QuotaUsagePerUser quotaUsagePerUser);

    public void setUnlimited() {
        percentageBarA.setStyleName(style.percentageBarUnlimited());
        percentageLabelA.setStyleName(style.percentageLabel());
        percentageLabelA.setText(constants.unlimitedQuota());
        percentageLabelA.setTitle(constants.unlimitedQuota());
        percentageBarB.setVisible(false);

    }

    public void setExceeded() {
        percentageBarA.setStyleName(style.percentageBarExceeded());
        percentageLabelA.setStyleName(style.percentageLabel());
        percentageLabelA.setText(constants.exceededQuota());
        percentageBarB.setVisible(false);
    }

    protected void setValues(double limit, double consumedByOthers, double consumedByUser) {

        int othersConsumptionPercent;
        int userConsumptionPercent;
        //Prevent potential divide by 0
        if (Math.round(limit) == 0) {
            othersConsumptionPercent = Integer.MAX_VALUE;
            userConsumptionPercent = Integer.MAX_VALUE;
        } else {
            othersConsumptionPercent = (int) Math.round(consumedByOthers * 100 / limit);
            userConsumptionPercent = (int) Math.round(consumedByUser * 100 / limit);
        }

        if (limit == UNLIMITED) { // unlimited
            setUnlimited();
        } else if (consumedByOthers + consumedByUser == 0) { // empty
            setZeroValue();
        } else if (consumedByOthers + consumedByUser > limit) { // exceeded
            setExceeded();
        } else {
            percentageBarA.setStyleName(style.percentageBarA());
            percentageLabelA.setStyleName(style.percentageLabelBlack());
            percentageBarB.setVisible(true);
            setValueA(othersConsumptionPercent);
            setValueB(userConsumptionPercent);
            setBars();
        }

        // update tooltip
        tooltip.setHtml(getTooltip());
        tooltip.reconfigure();
    }

    protected abstract SafeHtml getTooltip();

    protected SafeHtml templateWithLabels(String quota,
            int totalUsagePercentage, String totalUsage,
            int usedByYouPercentage, String usedByYou,
            int usedByOthersPercentage, String usedByOthers,
            int freePercentage, String free) {
        return templates.quotaForUserBarToolTip(constants.tooltipQuotaLabel(), quota,
                constants.tooltipTotalUsageLabel(), totalUsagePercentage, totalUsage,
                constants.youUseQuota(), usedByYouPercentage, usedByYou,
                constants.othersUseQuota(), usedByOthersPercentage, usedByOthers,
                constants.freeQuota(), freePercentage, free);
    }

}
