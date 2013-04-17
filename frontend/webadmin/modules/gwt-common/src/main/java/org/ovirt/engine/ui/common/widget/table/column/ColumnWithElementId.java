package org.ovirt.engine.ui.common.widget.table.column;

/**
 * Interface implemented by cell table {@link com.google.gwt.user.cellview.client.Column columns} whose cells render
 * HTML content with DOM element ID for better accessibility.
 */
public interface ColumnWithElementId {

    /**
     * Configure column content element ID options.
     *
     * @param elementIdPrefix
     *            DOM element ID prefix to use for the text container element (must not be {@code null}).
     * @param columnId
     *            Column ID that will be part of the resulting DOM element ID, or {@code null} to use column index
     *            value.
     */
    void configureElementId(String elementIdPrefix, String columnId);

}
