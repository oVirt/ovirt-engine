package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.AbstractRenderedTextColumn;
import org.ovirt.engine.ui.webadmin.widget.renderer.HumanReadableTimeRenderer;

public abstract class AbstractHumanReadableTimeColumn<T> extends AbstractRenderedTextColumn<T, Double> {

    public AbstractHumanReadableTimeColumn() {
        super(new HumanReadableTimeRenderer());
    }

}
