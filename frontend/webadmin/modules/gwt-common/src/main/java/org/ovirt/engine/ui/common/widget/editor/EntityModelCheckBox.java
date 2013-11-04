package org.ovirt.engine.ui.common.widget.editor;

/**
 * This class extends Composite instead of CheckBox because CheckBox is a Boolean type editor.
 * @deprecated use the org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBox instead
 */
public class EntityModelCheckBox extends BaseEntityModelCheckbox<Object> {

    @Override
    public Object getValue() {
        return asCheckBox().getValue();
    }

    @Override
    public void setValue(Object value) {
        asCheckBox().setValue((Boolean) value);
    }


}
