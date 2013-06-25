package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.SafeHtmlColumn;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Column for displaying generic progress bar.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class ProgressBarColumn<T> extends SafeHtmlColumn<T> {

    @Override
    public final SafeHtml getValue(T object) {
        Integer progressValue = getProgressValue(object);

        int progress = progressValue != null ? progressValue : 0;
        String text = getProgressText(object);

        // Choose color by progress
        String color = progress < 70 ? "#669966" : progress < 95 ? "#FF9900" : "#FF0000"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return ClientGinjectorProvider.getApplicationTemplates().progressBar(progress, text, color);
    }

    /**
     * Returns the progress value in percent ({@code null} values will be interpreted as zeroes).
     */
    protected abstract Integer getProgressValue(T object);

    /**
     * Returns the text to show within the progress bar.
     */
    protected abstract String getProgressText(T object);

}
