package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelLabel extends EntityModelLabel<String> {

    public StringEntityModelLabel() {
        super(new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser());
    }
}
