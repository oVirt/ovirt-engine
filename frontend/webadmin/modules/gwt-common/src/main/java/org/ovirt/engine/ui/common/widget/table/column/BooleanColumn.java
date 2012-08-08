package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.renderer.BooleanRenderer;


public abstract class BooleanColumn<T> extends RenderedTextColumn<T, Boolean> {

    public BooleanColumn(String trueText) {
        this(trueText, ""); //$NON-NLS-1$
    }

    public BooleanColumn(String trueText, String falseText) {
        super(new BooleanRenderer(trueText, falseText));
    }

}
