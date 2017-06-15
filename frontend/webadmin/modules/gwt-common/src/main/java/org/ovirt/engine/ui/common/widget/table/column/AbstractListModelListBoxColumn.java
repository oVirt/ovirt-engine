package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.ListModelListBoxCell;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.cellview.client.Column;

/**
 * Column for displaying a ListBox backed by ListModel using {@link ListModelListBoxCell}.
 *
 * @param <T>
 *            the row type.
 * @param <S>
 *            the ListModel item type.
 */
public abstract class AbstractListModelListBoxColumn<T, S> extends Column<T, ListModel> {

    public AbstractListModelListBoxColumn(Renderer<S> renderer) {
        super(new ListModelListBoxCell<>(renderer));
    }

}
