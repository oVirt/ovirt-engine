package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class DoubleEntityModelLabelEditor extends EntityModelLabelEditor<Double> {
    public DoubleEntityModelLabelEditor(Renderer<Double> renderer, Parser<Double> parser) {
        super(renderer, parser);
    }

    public DoubleEntityModelLabelEditor() {
        super(new DoubleEntityModelLabel());
    }
}
