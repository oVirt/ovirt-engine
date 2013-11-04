package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.editor.BaseEntityModelCheckboxEditor;

/**
 * Composite Editor that uses {@link EntityModelCheckBox}.
 */
public class EntityModelCheckBoxEditor extends BaseEntityModelCheckboxEditor<Boolean> {

    public EntityModelCheckBoxEditor() {
        super(new EntityModelCheckBox());
    }

    public EntityModelCheckBoxEditor(Align labelAlign) {
        super(labelAlign, new EntityModelCheckBox());
    }

    public EntityModelCheckBoxEditor(Align labelAlign, VisibilityRenderer visibilityRenderer) {
        super(labelAlign, new EntityModelCheckBox(), visibilityRenderer);
    }

    public EntityModelCheckBoxEditor(Align labelAlign, VisibilityRenderer visibilityRenderer, boolean useFullWidthIfAvailable) {
        super(labelAlign, new EntityModelCheckBox(), visibilityRenderer, useFullWidthIfAvailable);
    }

}
