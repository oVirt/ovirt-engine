package org.ovirt.engine.ui.userportal.widget;

public class QuotaProgressBar extends DoublePercentageProgressBar {

    public void setUnlimited(boolean unlimited) {
        if (unlimited) {
            percentageBarA.setStyleName(style.percentageBarUnlimited());
            percentageLabelA.setText("Unlimited"); //$NON-NLS-1$
            percentageLabelA.setTitle("Unlimited"); //$NON-NLS-1$
        } else {
            percentageBarA.setStyleName(style.percentageBarA());
        }
        percentageBarB.setVisible(!unlimited);
    }
}
