package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToIntEntityModelParser;

public class IntegerEntityModelLabel extends EntityModelLabel<Integer> {
    public IntegerEntityModelLabel() {
        super(new ToStringEntityModelRenderer<Integer>(), ToIntEntityModelParser.newTrimmingParser());
    }
}
