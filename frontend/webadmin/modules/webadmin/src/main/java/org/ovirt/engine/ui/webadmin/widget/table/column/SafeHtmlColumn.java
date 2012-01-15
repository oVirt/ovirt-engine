package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Column;

/**
 * Column for displaying {@link SafeHtml} instances.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class SafeHtmlColumn<T> extends Column<T, SafeHtml> {

    public SafeHtmlColumn() {
        super(new SafeHtmlCell());
    }

    protected ApplicationTemplates getApplicationTemplates() {
        return ClientGinjectorProvider.instance().getApplicationTemplates();
    }

}
