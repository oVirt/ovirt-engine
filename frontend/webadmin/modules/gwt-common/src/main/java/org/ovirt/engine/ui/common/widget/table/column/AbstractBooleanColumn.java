package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.BooleanRenderer;


public abstract class AbstractBooleanColumn<T> extends AbstractRenderedTextColumn<T, Boolean> {

    public AbstractBooleanColumn(String trueText) {
        this(trueText, ""); //$NON-NLS-1$
    }

    public AbstractBooleanColumn(String trueText, String falseText) {
        super(new BooleanRenderer(trueText, falseText));
    }

}
