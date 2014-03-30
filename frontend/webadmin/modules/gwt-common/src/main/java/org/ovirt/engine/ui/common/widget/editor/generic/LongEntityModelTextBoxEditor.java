package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToLongEntityParser;

public class LongEntityModelTextBoxEditor extends EntityModelTextBoxEditor<Long> {

    public LongEntityModelTextBoxEditor() {
        super(new ToStringEntityModelRenderer<Long>(), new ToLongEntityParser());
    }
}
