package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class StringEntityModelIconEditor extends EntityModelIconEditor<String> {

    public StringEntityModelIconEditor(Renderer<String> renderer, Parser<String> parser, String iconName) {
        super(renderer, parser, iconName);
    }

    public StringEntityModelIconEditor() {
        super(new StringEntityModelLabel(), "fa fa-user");//$NON-NLS-1$
        addStyleNameToIcon("user-icon-color");//$NON-NLS-1$
    }
}
