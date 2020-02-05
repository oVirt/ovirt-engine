package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

import com.google.gwt.text.shared.Parser;

public class StringEntityModelTextBox extends EntityModelTextBox<String> {

    public StringEntityModelTextBox() {
        this(new ToStringEntityModelParser());
    }

    public StringEntityModelTextBox(Parser<String> parser) {
        super(new ToStringEntityModelRenderer<String>(), parser);
    }

}
