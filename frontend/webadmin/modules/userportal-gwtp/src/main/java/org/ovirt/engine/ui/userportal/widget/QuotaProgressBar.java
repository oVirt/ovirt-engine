package org.ovirt.engine.ui.userportal.widget;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;

public class QuotaProgressBar extends DoublePercentageProgressBar {

    public static final int UNLIMITED = -1;
    private static final ApplicationMessages messages = GWT.create(ApplicationMessages.class);
    private static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);
    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<Number>(DiskSizeRenderer.DiskSizeUnit.GIGABYTE);

    private QuotaType type;
    private static final double GIGA = 1024;
    private static final double MB_GB_THRESHOLD = 4; // over this threshold number would be presented in GB not MB

    public QuotaProgressBar(QuotaType type) {
        this.type = type;
    }

    public void setUnlimited() {
        percentageBarA.setStyleName(style.percentageBarUnlimited());
        percentageLabelA.setStyleName(style.percentageLabel());
        percentageLabelA.setText("Unlimited"); //$NON-NLS-1$
        percentageLabelA.setTitle("Unlimited"); //$NON-NLS-1$
        percentageBarB.setVisible(false);

    }

    public void setExceeded(String title) {
        percentageBarA.setStyleName(style.percentageBarExceeded());
        percentageLabelA.setStyleName(style.percentageLabel());
        percentageLabelA.setText("Exceeded"); //$NON-NLS-1$
        percentageLabelA.setTitle(title);
        percentageBarB.setVisible(false);
    }

    public void setValues(double limit, double consumedByOthers, double consumedByUser) {

        int othersConsumptionPercent = (int) Math.round(consumedByOthers * 100 / limit);
        int userConsumptionPercent = (int) Math.round(consumedByUser * 100 / limit);
        double free = limit - consumedByOthers - consumedByUser;

        setTitleInternal(free);

        if (limit == UNLIMITED) { // unlimited
            setUnlimited();
        } else if (consumedByOthers + consumedByUser == 0) { // empty
            setZeroValue();
        } else if (consumedByOthers + consumedByUser > limit) { // exceeded
            switch (getType()) {
            case STORAGE:
                setExceeded(messages.exceedingStorage(othersConsumptionPercent + userConsumptionPercent - 100, -free));
                break;
            case CPU:
                setExceeded(messages.exceedingCpus(othersConsumptionPercent + userConsumptionPercent - 100, (int) -free));
                break;
            case MEM:
                String freeMem = free < (-MB_GB_THRESHOLD * GIGA) ? diskSizeRenderer.render(-free/GIGA) : (int) -free + "MB"; //$NON-NLS-1$
                setExceeded(messages.exceedingMem(othersConsumptionPercent + userConsumptionPercent - 100, freeMem));
                break;
            }
        } else {
            percentageBarA.setStyleName(style.percentageBarA());
            percentageLabelA.setStyleName(style.percentageLabelBlack());
            percentageBarB.setVisible(true);
            setValueA(othersConsumptionPercent);
            setValueB(userConsumptionPercent);
            setBars();
        }
    }

    private void setTitleInternal(double free) {
        switch (getType()) {
            case STORAGE:
                String freeStorage = free == 0 ? "0" : diskSizeRenderer.render(free); //$NON-NLS-1$
                setTitle(constants.freeStorage() + freeStorage);
                break;
            case CPU:
                setTitle(messages.quotaFreeCpus((int) free));
                break;
            case MEM:
                String freeMem = free > (MB_GB_THRESHOLD * GIGA) ? diskSizeRenderer.render(free/GIGA) : (int) free + "MB"; //$NON-NLS-1$
                setTitle(constants.freeMemory() + freeMem);
                break;
        }
    }

    public QuotaType getType() {
        return type;
    }

    public void setType(QuotaType type) {
        this.type = type;
    }

    public static enum QuotaType {
        STORAGE,
        CPU,
        MEM
    }

}
