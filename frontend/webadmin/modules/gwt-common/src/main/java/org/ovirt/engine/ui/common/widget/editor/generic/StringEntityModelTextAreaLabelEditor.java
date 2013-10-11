package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelTextAreaLabelEditor extends EntityModelTextAreaLabelEditor<String> {

    public StringEntityModelTextAreaLabelEditor() {
        super(new StringEntityModelTextAreaLabel());
    }

    public StringEntityModelTextAreaLabelEditor(boolean showBorder, boolean disableResizing) {
        super(showBorder, disableResizing, new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser());
    }
}
