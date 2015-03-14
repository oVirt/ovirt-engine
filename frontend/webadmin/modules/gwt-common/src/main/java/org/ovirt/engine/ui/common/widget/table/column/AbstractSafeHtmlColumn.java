package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.SafeHtmlCell;

import com.google.gwt.safehtml.shared.SafeHtml;

/**
 * Column for displaying {@link SafeHtml} instances. Supports tooltips.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractSafeHtmlColumn<T> extends AbstractColumn<T, SafeHtml> {

    public AbstractSafeHtmlColumn() {
        super(new SafeHtmlCell());
    }

    @Override
    public SafeHtmlCell getCell() {
        return (SafeHtmlCell) super.getCell();
    }

}
