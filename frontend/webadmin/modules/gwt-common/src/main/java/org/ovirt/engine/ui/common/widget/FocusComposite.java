package org.ovirt.engine.ui.common.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;

public class FocusComposite extends Composite {

    private boolean isFocused;

    protected void addFocusWidget(FocusWidget widget) {
        widget.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                isFocused = true;
            }
        });

        widget.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                isFocused = false;
            }
        });
    }

    public void setIsFocused(boolean isFocused) {
        this.isFocused = isFocused;

    }

    public boolean isFocused() {
        return isFocused;
    }

}
