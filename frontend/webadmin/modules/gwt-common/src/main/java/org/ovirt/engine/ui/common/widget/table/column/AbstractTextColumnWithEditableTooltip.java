package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.TextCellWithEditableTooltip;


/**
 * Column for displaying text using {@link TextCellWithEditableTooltip}.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractTextColumnWithEditableTooltip<T> extends AbstractSortableColumn<T, String>
    implements ColumnWithElementId {

    public AbstractTextColumnWithEditableTooltip() {
        super(new TextCellWithEditableTooltip());
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public TextCellWithEditableTooltip getCell() {
        return (TextCellWithEditableTooltip) super.getCell();
    }
}
