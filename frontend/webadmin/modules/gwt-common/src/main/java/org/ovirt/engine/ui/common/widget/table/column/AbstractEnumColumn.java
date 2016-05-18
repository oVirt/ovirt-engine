package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;

import com.google.gwt.cell.client.FieldUpdater;

/**
 * Column for displaying Enum values.
 *
 * @param <T>
 *            Table row data type.
 * @param <E>
 *            Enum type.
 */
public abstract class AbstractEnumColumn<T, E extends Enum<E>> extends AbstractRenderedTextColumn<T, E> {

    public AbstractEnumColumn() {
        super(new EnumRenderer<E>());
    }

    public AbstractEnumColumn(FieldUpdater<T, String> fieldUpdater) {
        this();
        setFieldUpdater(fieldUpdater);
    }
}
