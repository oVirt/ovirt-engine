package org.ovirt.engine.ui.common.utils;

import com.google.gwt.cell.client.Cell;

public class ElementIdUtils {

    /**
     * Returns DOM element ID, based on prefix and custom (dynamic) value.
     *
     * @param prefix
     *            Element ID prefix that meets ID constraints (unique, deterministic).
     * @param value
     *            Custom value used to extend the prefix.
     */
    public static String createElementId(String prefix, String value) {
        String sanitizedValue = value.replaceAll("[^\\w]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
        return prefix + "_" + sanitizedValue; //$NON-NLS-1$
    }

    /**
     * Returns DOM element ID for a table cell, using cell context object.
     *
     * @param prefix
     *            Element ID prefix that meets ID constraints (unique, deterministic).
     * @param columnId
     *            Column ID that will be part of the resulting DOM element ID, or {@code null} to use column index
     *            value.
     * @param context
     *            Table cell context object.
     */
    public static String createTableCellElementId(String prefix, String columnId, Cell.Context context) {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append("_"); //$NON-NLS-1$
        sb.append(columnId != null ? columnId : "col" + String.valueOf(context.getColumn())); //$NON-NLS-1$
        sb.append("_row"); //$NON-NLS-1$
        sb.append(String.valueOf(context.getIndex()));
        return sb.toString();
    }

}
