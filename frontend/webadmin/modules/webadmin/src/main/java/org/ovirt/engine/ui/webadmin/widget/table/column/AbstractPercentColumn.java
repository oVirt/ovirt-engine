package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractProgressBarColumn;

/**
 * Column for displaying percent-based progress bar.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractPercentColumn<T> extends AbstractProgressBarColumn<T> {

    @Override
    protected String getProgressText(T object) {
        Integer progressValue = getProgressValue(object);
        return progressValue != null ? progressValue + "%" : "0%"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
