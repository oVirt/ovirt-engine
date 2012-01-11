package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.user.client.ui.CustomButton;

public abstract class AbstractDialogButton extends CustomButton {

    protected String text;

    protected AbstractDialogButton(String text) {
        super();
        this.text = text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
        updateFaces();
    }

    protected abstract void updateFaces();

    @Override
    protected void onClick() {
        setDown(false);
        super.onClick();
    }

    @Override
    protected void onClickCancel() {
        setDown(false);
    }

    @Override
    protected void onClickStart() {
        setDown(true);
    }

}
