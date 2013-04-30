package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelTextAreaEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextArea> {

    public EntityModelTextAreaEditor() {
        super(new EntityModelTextArea());
    }

    public EntityModelTextAreaEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextArea(renderer, parser));
    }
}
