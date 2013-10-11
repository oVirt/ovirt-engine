package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import org.ovirt.engine.ui.common.widget.editor.AbstractValueBoxWithLabelEditor;

public class EntityModelTextAreaEditor<T> extends AbstractValueBoxWithLabelEditor<T, EntityModelTextArea<T>> {

    public EntityModelTextAreaEditor(Renderer<T> renderer, Parser<T> parser) {
        this(new EntityModelTextArea<T>(renderer, parser));
    }

    public EntityModelTextAreaEditor(EntityModelTextArea<T> widget) {
        super(widget);
    }
}
