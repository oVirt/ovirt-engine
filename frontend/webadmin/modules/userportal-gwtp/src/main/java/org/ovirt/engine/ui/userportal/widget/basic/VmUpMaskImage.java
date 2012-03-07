package org.ovirt.engine.ui.userportal.widget.basic;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;

public class VmUpMaskImage extends Image implements IsEditor<TakesValueEditor<Boolean>>, TakesValue<Boolean> {

    @Override
    public void setValue(Boolean value) {
        setVisible(!value);
    }

    @Override
    public Boolean getValue() {
        return isVisible();
    }

    @Override
    public TakesValueEditor<Boolean> asEditor() {
        return TakesValueEditor.of(this);
    }

}
