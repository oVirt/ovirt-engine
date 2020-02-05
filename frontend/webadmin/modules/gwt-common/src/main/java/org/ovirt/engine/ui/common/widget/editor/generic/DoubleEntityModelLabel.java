package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToDoubleEntityModelParser;

public class DoubleEntityModelLabel extends EntityModelLabel<Double> {
    public DoubleEntityModelLabel() {
        super(new ToStringEntityModelRenderer<Double>(), ToDoubleEntityModelParser.newTrimmingParser());
    }
}
