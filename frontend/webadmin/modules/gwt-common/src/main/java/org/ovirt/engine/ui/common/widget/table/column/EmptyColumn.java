package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.user.cellview.client.TextColumn;

public class EmptyColumn<T> extends TextColumn<T> {
    @Override
    public String getValue(T object) {
        return null;
    }
}
