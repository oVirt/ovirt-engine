package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.view.SubTabHelper;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

public class MainContentView extends AbstractView implements MainContentPresenter.ViewDef {
    private static final int SPLITTER_THICKNESS = 4;

    private final SplitLayoutPanel splitPanel;
    private final SimplePanel mainTabPanelContainer = new SimplePanel();
    private final SimplePanel subTabPanelContainer = new SimplePanel();
    private final ClientStorage clientStorage;
    private boolean subTabPanelVisible;

    @Inject
    public MainContentView(final ClientStorage clientStorage) {
        splitPanel = new SplitLayoutPanel(SPLITTER_THICKNESS) {
            @Override
            public void onResize() {
                super.onResize();
                if (subTabPanelVisible) {
                    SubTabHelper.storeSubTabHeight(clientStorage, subTabPanelContainer);
                }
            }
        };
        this.clientStorage = clientStorage;
        initWidget(splitPanel);
        initSplitPanel();
    }

    void initSplitPanel() {
        splitPanel.add(mainTabPanelContainer);
        subTabPanelVisible = false;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
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
                splitPanel.addSouth(subTabPanelContainer, SubTabHelper.getSubTabHeight(clientStorage, splitPanel));
                splitPanel.add(mainTabPanelContainer);
            } else {
                splitPanel.add(mainTabPanelContainer);
            }

            this.subTabPanelVisible = subTabPanelVisible;
        }
    }

}
