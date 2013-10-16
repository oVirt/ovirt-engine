package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.RenderedTextColumn;
import org.ovirt.engine.ui.webadmin.widget.renderer.HumanReadableTimeRenderer;

public abstract class HumanReadableTimeColumn<T> extends RenderedTextColumn<T, Double> {

    public HumanReadableTimeColumn() {
        super(new HumanReadableTimeRenderer());
    }

}
