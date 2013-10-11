package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelTextArea extends EntityModelTextArea<String> {

    public StringEntityModelTextArea() {
        super(new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser());
    }
}
