package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.editor.AbstractValueBoxWithLabelEditor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

/**
 * Composite Editor that uses {@link EntityModelTextBox}.
 */
public class EntityModelTextBoxEditor<T> extends AbstractValueBoxWithLabelEditor<T, EntityModelTextBox<T>> {

    public EntityModelTextBoxEditor(EntityModelTextBox<T> contentWidget, VisibilityRenderer visibilityRenderer) {
        super(contentWidget, visibilityRenderer);
    }

    public EntityModelTextBoxEditor(Renderer<T> renderer, Parser<T> parser) {
        super(new EntityModelTextBox<>(renderer, parser));
    }

    public EntityModelTextBoxEditor(Renderer<T> renderer, Parser<T> parser, VisibilityRenderer visibilityRenderer) {
        super(new EntityModelTextBox<>(renderer, parser), visibilityRenderer);
    }
}
