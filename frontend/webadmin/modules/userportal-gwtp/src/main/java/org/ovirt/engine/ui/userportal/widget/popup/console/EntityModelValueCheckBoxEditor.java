package org.ovirt.engine.ui.userportal.widget.popup.console;

import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.BaseEntityModelCheckboxEditor;
import org.ovirt.engine.ui.userportal.widget.popup.console.EntityModelValueCheckbox.ValueCheckboxRenderer;

public class EntityModelValueCheckBoxEditor<T> extends BaseEntityModelCheckboxEditor<T> {

    public EntityModelValueCheckBoxEditor(ValueCheckboxRenderer<T> renderer) {
        this(Align.LEFT, renderer);
    }

    public EntityModelValueCheckBoxEditor(Align labelAlign, ValueCheckboxRenderer<T> renderer) {
        super(labelAlign, new EntityModelValueCheckbox<T>(renderer));
    }

}
