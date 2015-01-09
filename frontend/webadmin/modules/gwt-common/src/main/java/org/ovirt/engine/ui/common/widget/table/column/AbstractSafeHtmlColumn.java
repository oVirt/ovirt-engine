package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Column for displaying {@link SafeHtml} instances.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractSafeHtmlColumn<T> extends AbstractSortableColumn<T, SafeHtml> {

    public AbstractSafeHtmlColumn() {
        super(new SafeHtmlCell());
    }

}
