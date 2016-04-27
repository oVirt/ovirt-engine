package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.parser.generic.ToStringEntityModelParser;

public class StringEntityModelPasswordBoxIconEditor extends EntityModelPasswordBoxIconEditor<String> {

    public StringEntityModelPasswordBoxIconEditor() {
        super(new ToStringEntityModelRenderer<String>(), new ToStringEntityModelParser(), "fa fa-key");//$NON-NLS-1$
        addStyleNameToIcon("psw-icon-color");//$NON-NLS-1$
    }
}
