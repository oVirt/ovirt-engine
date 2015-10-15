package org.ovirt.engine.ui.webadmin.widget.table.column;


import org.ovirt.engine.ui.common.widget.table.column.AbstractProgressBarColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

/**
 * Column for displaying percent-based progress bar for quota.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractQuotaPercentColumn<T> extends AbstractProgressBarColumn<T> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    protected String getProgressText(T object) {
        Integer progressValue = getProgressValue(object);
        if (progressValue != null && progressValue > 100) {
            return constants.exceeded();
        }
        if (progressValue != null && progressValue < 0) {
            return constants.unlimited();
        }
        return progressValue != null ? progressValue + "%" : "0%"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
