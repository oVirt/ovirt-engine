package org.ovirt.engine.ui.uicommonweb;

import com.google.gwt.event.shared.GwtEvent;

public interface ErrorPopupManager {

    public void fireEvent(GwtEvent<?> event);
    public void show(String errorMessage);
    public void hide();
}
