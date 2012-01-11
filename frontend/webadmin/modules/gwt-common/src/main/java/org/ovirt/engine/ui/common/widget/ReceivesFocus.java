package org.ovirt.engine.ui.common.widget;

/**
 * Widgets that implement this interface receive focus events.
 */
public interface ReceivesFocus {

    void onFocus();

    void onBlur();

}
