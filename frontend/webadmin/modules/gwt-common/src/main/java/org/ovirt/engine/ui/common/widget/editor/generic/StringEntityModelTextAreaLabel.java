package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelTextAreaLabel extends EntityModelTextAreaLabel<String> {

    public StringEntityModelTextAreaLabel() {
        super(new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser());
    }
}
