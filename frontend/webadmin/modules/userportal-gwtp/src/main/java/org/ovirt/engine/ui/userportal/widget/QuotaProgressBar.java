package org.ovirt.engine.ui.userportal.widget;

import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.ui.common.utils.PopupUtils;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;

public abstract class QuotaProgressBar extends DoublePercentageProgressBar implements HasMouseOutHandlers, HasMouseOverHandlers, MouseOutHandler, MouseOverHandler {

    public static final int UNLIMITED = -1;
    private static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);
    protected static final SafeHtml EMPTY_HTML = new SafeHtml() {
        @Override
        public String asString() {
            return ""; //$NON-NLS-1$
        }
    };
    private final HTML tooltip = new HTML();
    private final DecoratedPopupPanel tooltipPanel = new DecoratedPopupPanel();
    private final ApplicationTemplates templates = GWT.create(ApplicationTemplates.class);

    protected QuotaUsagePerUser quotaUsagePerUser;

    public QuotaProgressBar(QuotaUsagePerUser quotaUsagePerUser) {
        setQuotaUsagePerUser(quotaUsagePerUser);
        initToolTip();
    }

    public QuotaProgressBar() {
        initToolTip();
    }

    private void initToolTip() {
        tooltipPanel.setWidget(tooltip);
        addMouseOutHandler(this);
        addMouseOverHandler(this);
    }

    public void setQuotaUsagePerUser(QuotaUsagePerUser quotaUsagePerUser) {
        this.quotaUsagePerUser = quotaUsagePerUser;
        setValuesByType(quotaUsagePerUser);
    }

    protected abstract void setValuesByType(QuotaUsagePerUser quotaUsagePerUser);

    public void setUnlimited() {
        percentageBarA.setStyleName(style.percentageBarUnlimited());
        percentageLabelA.setStyleName(style.percentageLabel());
        percentageLabelA.setText("Unlimited"); //$NON-NLS-1$
        percentageLabelA.setTitle("Unlimited"); //$NON-NLS-1$
        percentageBarB.setVisible(false);

    }

    public void setExceeded() {
        percentageBarA.setStyleName(style.percentageBarExceeded());
        percentageLabelA.setStyleName(style.percentageLabel());
        percentageLabelA.setText("Exceeded"); //$NON-NLS-1$
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
    }

    @Override
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    @Override
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        tooltipPanel.hide(true);
    }

    @Override
    public void onMouseOver(MouseOverEvent event) {
        SafeHtml tooltipHtml = getTooltip();
        if (!"".equals(tooltipHtml.asString())) { //$NON-NLS-1$
            tooltip.setHTML(tooltipHtml);
            PopupUtils.adjustPopupLocationToFitScreenAndShow(tooltipPanel, event.getClientX(), event.getClientY() + 20);
        }
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

    @Override
    protected void onDetach() {
        super.onDetach();
        tooltipPanel.hide(true);
    }
}
