package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractTabPanelView;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.gwtplatform.mvp.client.HasUiHandlers;


public abstract class AbstractSubTabPanelView extends AbstractTabPanelView implements HasUiHandlers<TabWidgetHandler> {

    protected AbstractSubTabPanelView() {
        generateIds();
    }

    protected void setTabBar(IsWidget content) {
        AbstractTabPanel panel = getTabPanel();
        if (panel instanceof SimpleTabPanel) {
            ((SimpleTabPanel) getTabPanel()).setTabBar(content);
        }
    }

    protected abstract void generateIds();

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AbstractSubTabPanelPresenter.TYPE_SetTabBar) {
            setTabBar(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setUiHandlers(TabWidgetHandler uiHandlers) {
        AbstractTabPanel panel = getTabPanel();
        if (panel instanceof SimpleTabPanel) {
            ((SimpleTabPanel) getTabPanel()).setUiHandlers(uiHandlers);
        }
    }
}
