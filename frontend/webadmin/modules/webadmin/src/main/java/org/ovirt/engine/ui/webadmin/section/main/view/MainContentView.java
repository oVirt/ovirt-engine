package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;
import org.ovirt.engine.ui.webadmin.view.AbstractView;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainContentView extends AbstractView implements MainContentPresenter.ViewDef {

    private static final int subTabPanelHeight = 300;

    private final SplitLayoutPanel splitPanel = new SplitLayoutPanel();
    private final SimplePanel mainTabPanelContainer = new SimplePanel();
    private final SimplePanel subTabPanelContainer = new SimplePanel();

    public MainContentView() {
        initWidget(splitPanel);
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == MainContentPresenter.TYPE_SetMainTabPanelContent)
            setPanelContent(mainTabPanelContainer, content);
        else if (slot == MainContentPresenter.TYPE_SetSubTabPanelContent)
            setPanelContent(subTabPanelContainer, content);
        else
            super.setInSlot(slot, content);
    }

    @Override
    public void update(boolean subTabPanelVisible) {
        splitPanel.clear();

        if (subTabPanelVisible) {
            splitPanel.addSouth(subTabPanelContainer, subTabPanelHeight);
            splitPanel.add(mainTabPanelContainer);
        } else {
            splitPanel.add(mainTabPanelContainer);
        }
    }

}
