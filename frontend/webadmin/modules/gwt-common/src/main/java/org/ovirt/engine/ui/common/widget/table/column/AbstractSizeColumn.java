package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import com.google.gwt.text.shared.Renderer;

public abstract class AbstractSizeColumn<T> extends AbstractRenderedTextColumn<T, Long> {

    public AbstractSizeColumn(Renderer<Long> renderer) {
        super(renderer);
    }

    @Override
    public void makeSortable() {
        makeSortable(Comparator.nullsFirst(Comparator.comparing(this::getRawValue)));
    }

}
