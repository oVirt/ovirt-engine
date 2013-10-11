package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelPasswordBox extends EntityModelPasswordBox<String> {

    public StringEntityModelPasswordBox() {
        super(new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser());
    }
}
