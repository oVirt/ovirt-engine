package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelTextBox extends EntityModelTextBox<String> {

    public StringEntityModelTextBox() {
        super(new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser());
    }

}
