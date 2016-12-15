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
                Long val1 = getRawValue(object1);
                Long val2 = getRawValue(object2);

                if (val1 == null) {
                    return (val2 == null) ? 0 : -1;
                } else if (val2 == null) {
                    return 1;
                }

                return val1.compareTo(val2);
            }
        });
    }

}
