package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;

public class VmPausedImage extends Image implements IsEditor<TakesValueEditor<VMStatus>>, TakesValue<VMStatus> {

    private VMStatus status;

    @Override
    public void setValue(VMStatus status) {
        this.status = status;
        boolean paused = status.equals(VMStatus.Suspended) || status.equals(VMStatus.Paused);
        setVisible(paused);
    }

    @Override
    public VMStatus getValue() {
        return status;
    }

    @Override
    public TakesValueEditor<VMStatus> asEditor() {
        return TakesValueEditor.of(this);
    }

}
