package org.ovirt.engine.ui.webadmin.widget.table.column;


import org.ovirt.engine.ui.webadmin.ApplicationConstants;

/**
 * Column for displaying percent-based progress bar for quota.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractQuotaPercentColumn<T> extends AbstractProgressBarColumn<T> {

    public abstract ApplicationConstants getaApplicationConstants();

    @Override
    protected String getProgressText(T object) {
        Integer progressValue = getProgressValue(object);
        if (progressValue != null && progressValue > 100) {
            return getaApplicationConstants().exceeded();
        }
        if (progressValue != null && progressValue < 0) {
            return getaApplicationConstants().unlimited();
        }
        return progressValue != null ? progressValue + "%" : "0%"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
