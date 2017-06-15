package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.editor.AbstractValueBoxWithLabelEditor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

/**
 * Composite Editor that uses {@link EntityModelPasswordBox}.
 */
public class EntityModelPasswordBoxEditor<T> extends AbstractValueBoxWithLabelEditor<T, EntityModelPasswordBox<T>> {

    public EntityModelPasswordBoxEditor(Renderer<T> renderer, Parser<T> parser) {
        super(new EntityModelPasswordBox<>(renderer, parser));
    }

}
