package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.Align;

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

}
