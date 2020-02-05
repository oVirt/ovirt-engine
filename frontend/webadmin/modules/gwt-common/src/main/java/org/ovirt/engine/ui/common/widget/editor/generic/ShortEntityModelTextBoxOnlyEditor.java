package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToShortEntityModelParser;

public class ShortEntityModelTextBoxOnlyEditor extends NumberEntityModelTextBoxOnlyEditor<Short> {
    public ShortEntityModelTextBoxOnlyEditor() {
        super(new ToStringEntityModelRenderer<Short>(), ToShortEntityModelParser.newTrimmingParser());
    }
}
