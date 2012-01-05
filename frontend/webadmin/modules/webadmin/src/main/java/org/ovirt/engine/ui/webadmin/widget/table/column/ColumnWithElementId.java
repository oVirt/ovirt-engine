package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.user.cellview.client.Column;

/**
 * Interface implemented by cell table {@link Column columns} whose cells render HTML content with DOM element ID for
 * better accessibility.
 */
public interface ColumnWithElementId {

    /**
     * Configure column content element ID options.
     * 
     * @param elementIdPrefix
     *            DOM element ID prefix to use for the text container element (must not be {@code null}).
     * @param columnId
     *            Column ID that will be part of the resulting DOM element ID for the text container element, or
     *            {@code null} to use column index value.
     */
    public void configureElementId(String elementIdPrefix, String columnId);

}
