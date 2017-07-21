package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;

import com.gwtplatform.mvp.client.TabData;

public abstract class AbstractSubTabPanelView extends AbstractTabPanelView implements AbstractSubTabPanelPresenter.ViewDef {

    protected AbstractSubTabPanelView() {
        generateIds();
    }

    protected abstract void generateIds();

    @Override
    public void setTabVisible(TabData tabData, boolean visible) {
        getTabPanel().setTabVisible(tabData, visible);
    }

}
