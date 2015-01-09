package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.ScrollableTextCell;

import com.google.gwt.user.cellview.client.Column;

public abstract class AbstractScrollableTextColumn<T> extends Column<T, String> {

    public AbstractScrollableTextColumn() {
        super(new ScrollableTextCell());
    }
}
