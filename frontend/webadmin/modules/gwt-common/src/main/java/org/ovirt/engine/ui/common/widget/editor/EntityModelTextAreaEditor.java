package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

/**
 * @deprecated use the org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextAreaEditor instead
 */
@Deprecated
public class EntityModelTextAreaEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextArea> {

    public EntityModelTextAreaEditor() {
        super(new EntityModelTextArea());
    }

    public EntityModelTextAreaEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextArea(renderer, parser));
    }
}
