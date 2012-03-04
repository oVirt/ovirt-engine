package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;

public class EntityModelLabelEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelLabel> {

    /**
     * A ValueBoxWithLabelEditor that has a Label as the widget
     */
    public EntityModelLabelEditor() {
        super(new EntityModelLabel());
    }

    public EntityModelLabelEditor(Renderer<Object> renderer, Parser<Object> parser) {
        super(new EntityModelLabel(renderer, parser));
    }
}
