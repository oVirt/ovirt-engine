package org.ovirt.engine.ui.webadmin.widget.table.column;

public abstract class AbstractOneColorPercentColumn<T> extends AbstractPercentColumn<T> {

    private ProgressBarColors color;

    protected AbstractOneColorPercentColumn(ProgressBarColors color) {
        this.color = color;
    }

    @Override
    protected String getColorByProgress(int progress) {
        return color.asCode();
    }
}
