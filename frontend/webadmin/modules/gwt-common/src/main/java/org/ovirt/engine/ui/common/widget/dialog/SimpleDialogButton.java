package org.ovirt.engine.ui.common.widget.dialog;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.user.client.ui.HasText;

public class SimpleDialogButton extends Button implements HasText {

    public SimpleDialogButton() {
        this(""); //$NON-NLS-1$
    }

    protected SimpleDialogButton(String text) {
        super(text);
        setType(ButtonType.DEFAULT);
        setSize(ButtonSize.DEFAULT);
    }

    public void setCustomContentStyle(String customStyle) {
        addStyleName(customStyle);
    }

    public void setAsPrimary() {
        setType(ButtonType.PRIMARY);
    }
}
