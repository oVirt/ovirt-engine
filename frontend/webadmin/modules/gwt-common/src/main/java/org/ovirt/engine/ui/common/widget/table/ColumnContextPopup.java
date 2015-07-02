package org.ovirt.engine.ui.common.widget.table;

import org.ovirt.engine.ui.common.widget.PopupPanel;

/**
 * {@link PopupPanel} adapted for use with {@link ColumnContextMenu}.
 *
 * @param <T>
 *            Table row data type.
 */
public class ColumnContextPopup<T> extends PopupPanel {

    private final ColumnContextMenu<T> contextMenu;

    public ColumnContextPopup(ColumnController<T> controller) {
        super(true);
        this.contextMenu = new ColumnContextMenu<>(controller);
        setWidget(contextMenu);
    }

    public ColumnContextMenu<T> getContextMenu() {
        return contextMenu;
    }

}
