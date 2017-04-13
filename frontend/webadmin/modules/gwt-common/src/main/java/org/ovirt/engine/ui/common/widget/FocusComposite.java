package org.ovirt.engine.ui.common.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;

public class FocusComposite extends Composite {

    private boolean isFocused;

    protected void addFocusWidget(FocusWidget widget) {
        widget.addFocusHandler(event -> isFocused = true);

        widget.addBlurHandler(event -> isFocused = false);
    }

    public void setIsFocused(boolean isFocused) {
        this.isFocused = isFocused;

    }

    public boolean isFocused() {
        return isFocused;
    }

}
