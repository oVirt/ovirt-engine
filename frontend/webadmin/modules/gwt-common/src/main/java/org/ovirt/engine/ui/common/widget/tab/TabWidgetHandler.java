package org.ovirt.engine.ui.common.widget.tab;

import com.gwtplatform.mvp.client.UiHandlers;

public interface TabWidgetHandler extends UiHandlers {

    void addTabWidget(TabDefinition tab, int index);

    void updateTab(TabDefinition tab);

    void setActiveTab(TabDefinition tab);

}
