package org.ovirt.engine.ui.common.widget.table;

import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;

/**
 * Convenience interface for being able to add columns to our grid widgets that are
 * both Panels and Tables.
 */
public interface HasColumns<T> {

    public abstract void addColumn(Column<T, ?> column, String headerText);

    public abstract void addColumn(Column<T, ?> column, String headerText, String width);

    public abstract void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml);

    public abstract void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml, String width);

    public abstract void addColumn(Column<T, ?> column, SafeHtmlHeader header);

    public abstract void addColumn(Column<T, ?> column, SafeHtmlHeader header, String width);

}
