package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class IntegerEntityModelLabelEditor extends NumberEntityModelLabelEditor<Integer> {
    public IntegerEntityModelLabelEditor(Renderer<Integer> renderer, Parser<Integer> parser) {
        super(renderer, parser);
    }

    public IntegerEntityModelLabelEditor() {
        super(new IntegerEntityModelLabel());
    }
}
