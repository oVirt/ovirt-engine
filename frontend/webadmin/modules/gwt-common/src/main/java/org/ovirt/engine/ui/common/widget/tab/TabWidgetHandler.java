package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.user.client.ui.IsWidget;
import com.gwtplatform.mvp.client.UiHandlers;

public interface TabWidgetHandler extends UiHandlers {
    void addTabWidget(IsWidget tabWidget, int index);

    void removeTabWidget(IsWidget tabWidget);
}
