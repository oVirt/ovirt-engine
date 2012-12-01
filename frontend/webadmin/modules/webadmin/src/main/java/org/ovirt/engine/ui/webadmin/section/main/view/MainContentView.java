package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainContentView extends AbstractView implements MainContentPresenter.ViewDef {

    private static final int subTabPanelHeight = 300;

    private final SplitLayoutPanel splitPanel = new SplitLayoutPanel(4);
    private final SimplePanel mainTabPanelContainer = new SimplePanel();
    private final SimplePanel subTabPanelContainer = new SimplePanel();

    private boolean subTabPanelVisible;

    public MainContentView() {
        initWidget(splitPanel);
        initSplitPanel();
    }

    void initSplitPanel() {
        splitPanel.add(mainTabPanelContainer);
        subTabPanelVisible = false;
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == MainContentPresenter.TYPE_SetMainTabPanelContent) {
            setPanelContent(mainTabPanelContainer, content);
        } else if (slot == MainContentPresenter.TYPE_SetSubTabPanelContent) {
            setPanelContent(subTabPanelContainer, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setSubTabPanelVisible(boolean subTabPanelVisible) {
        if (this.subTabPanelVisible != subTabPanelVisible) {
            splitPanel.clear();

            if (subTabPanelVisible) {
                splitPanel.addSouth(subTabPanelContainer, subTabPanelHeight);
                splitPanel.add(mainTabPanelContainer);
            } else {
                splitPanel.add(mainTabPanelContainer);
            }

            this.subTabPanelVisible = subTabPanelVisible;
        }
    }

}
