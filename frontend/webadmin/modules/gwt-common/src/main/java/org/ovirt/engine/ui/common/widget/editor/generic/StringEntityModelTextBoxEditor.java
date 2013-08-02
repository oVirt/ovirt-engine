package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

/**
 * Composite Editor that uses {@link org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBox}.
 */
public class StringEntityModelTextBoxEditor extends EntityModelTextBoxEditor<String> {

    public StringEntityModelTextBoxEditor(VisibilityRenderer visibilityRenderer) {
        super(new StringEntityModelTextBox(), visibilityRenderer);
    }

    public StringEntityModelTextBoxEditor() {
        super(new StringEntityModelTextBox(), new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public StringEntityModelTextBoxEditor(Renderer<String> renderer, Parser<String> parser) {
        super(renderer, parser, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public StringEntityModelTextBoxEditor(Renderer<String> renderer, Parser<String> parser, VisibilityRenderer visibilityRenderer) {
        super(renderer, parser, visibilityRenderer);
    }
}
