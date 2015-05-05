package org.ovirt.engine.ui.common.widget.table.column;


import org.ovirt.engine.ui.common.widget.table.cell.DataurlImageCell;

public abstract class AbstractDataurlImageColumn<T> extends AbstractColumn<T, String> {

    public AbstractDataurlImageColumn() {
        super(new DataurlImageCell());
    }
}
