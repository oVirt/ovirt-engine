package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import com.google.gwt.text.shared.Renderer;

public abstract class AbstractSizeColumn<T> extends AbstractRenderedTextColumn<T, Long> {

    public AbstractSizeColumn(Renderer<Long> renderer) {
        super(renderer);
    }

    @Override
    public void makeSortable() {
        makeSortable(new Comparator<T>() {
            @Override
            public int compare(T object1, T object2) {
                return getRawValue(object1).compareTo(getRawValue(object2));
            }
        });
    }

}
