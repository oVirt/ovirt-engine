package org.ovirt.engine.ui.webadmin.widget.table.column;

public abstract class OneColorPercentColumn<T> extends PercentColumn<T> {

    private ProgressBarColors color;

    protected OneColorPercentColumn(ProgressBarColors color) {
        this.color = color;
    }

    @Override
    protected String getColorByProgress(int progress) {
        return color.asCode();
    }
}
