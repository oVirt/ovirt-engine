package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class StringEntityModelLabelEditor extends EntityModelLabelEditor<String> {

    public StringEntityModelLabelEditor(Renderer<String> renderer, Parser<String> parser) {
        super(renderer, parser);
    }

    public StringEntityModelLabelEditor() {
        super(new StringEntityModelLabel());
    }
}
