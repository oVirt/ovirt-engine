package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

/**
 * Composite Editor that uses {@link EntityModelCheckBox}.
 */
public class EntityModelCheckBoxEditor extends BaseEntityModelCheckboxEditor<Object> {

    public EntityModelCheckBoxEditor() {
        super(new EntityModelCheckBox());
    }

    public EntityModelCheckBoxEditor(Align labelAlign) {
        super(labelAlign, new EntityModelCheckBox());
    }

    public EntityModelCheckBoxEditor(Align labelAlign, VisibilityRenderer visibilityRenderer) {
        super(labelAlign, new EntityModelCheckBox(), visibilityRenderer);
    }

}
