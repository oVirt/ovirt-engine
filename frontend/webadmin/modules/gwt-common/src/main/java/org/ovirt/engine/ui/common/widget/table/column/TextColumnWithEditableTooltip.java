package org.ovirt.engine.ui.common.widget.table.column;


/**
 * Column for displaying text using {@link TextCellWithEditableTooltip}.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class TextColumnWithEditableTooltip<T> extends SortableColumn<T, String>
    implements ColumnWithElementId {

    public TextColumnWithEditableTooltip() {
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
