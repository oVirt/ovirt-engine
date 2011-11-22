package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.user.cellview.client.Column;

public abstract class ScrollableTextColumn<T> extends Column<T, String> {

    public ScrollableTextColumn() {
        super(new ScrollableTextCell());
    }
}
