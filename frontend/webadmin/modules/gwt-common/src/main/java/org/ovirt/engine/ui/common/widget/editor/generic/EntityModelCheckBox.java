package org.ovirt.engine.ui.common.widget.editor.generic;

import org.ovirt.engine.ui.common.widget.editor.BaseEntityModelCheckbox;

/**
 * This class extends Composite instead of CheckBox because CheckBox is a Boolean type editor.
 */
public class EntityModelCheckBox extends BaseEntityModelCheckbox<Boolean> {

    @Override
    public Boolean getValue() {
        return asCheckBox().getValue();
    }

    @Override
    public void setValue(Boolean value) {
        asCheckBox().setValue(value);
    }


}
