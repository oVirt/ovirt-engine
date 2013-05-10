package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

/**
 * Composite Editor that uses {@link EntityModelTextBox}.
 */
public class EntityModelTextBoxEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextBox> {

    public EntityModelTextBoxEditor(VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox(), visibilityRenderer);
    }

    public EntityModelTextBoxEditor() {
        super(new EntityModelTextBox());
    }

    public EntityModelTextBoxEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextBox(renderer, parser));
    }

    public EntityModelTextBoxEditor(Renderer<Object> renderer, Parser<Object> parser, VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox(renderer, parser), visibilityRenderer);
    }
}
