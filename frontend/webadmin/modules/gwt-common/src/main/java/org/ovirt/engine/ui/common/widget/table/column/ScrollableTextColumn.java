package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.ScrollableTextCell;

import com.google.gwt.user.cellview.client.Column;

public abstract class ScrollableTextColumn<T> extends Column<T, String> {

    public ScrollableTextColumn() {
        super(new ScrollableTextCell());
    }
}
