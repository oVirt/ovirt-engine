package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import org.ovirt.engine.ui.common.widget.editor.AbstractValueBoxWithLabelEditor;

public class EntityModelLabelEditor<T> extends AbstractValueBoxWithLabelEditor<T, EntityModelLabel<T>> {

    /**
     * A ValueBoxWithLabelEditor that has a Label as the widget
     */
    public EntityModelLabelEditor(Renderer<T> renderer, Parser<T> parser) {
        this(new EntityModelLabel<T>(renderer, parser));
    }

    public EntityModelLabelEditor(EntityModelLabel<T> widget) {
        super(widget);
    }
}
