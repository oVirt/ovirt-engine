package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelTextAreaLabelEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelTextAreaLabel> {

    /**
     * A ValueBoxWithLabelEditor that has a Label as the widget
     */
    public EntityModelTextAreaLabelEditor() {
        super(new EntityModelTextAreaLabel());
    }

    public EntityModelTextAreaLabelEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelTextAreaLabel(renderer, parser));
    }
}
