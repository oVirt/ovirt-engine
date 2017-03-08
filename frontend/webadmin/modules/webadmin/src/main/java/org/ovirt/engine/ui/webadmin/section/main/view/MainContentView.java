package org.ovirt.engine.ui.webadmin.section.main.view;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class MainContentView extends AbstractView implements MainContentPresenter.ViewDef {

    private final FlowPanel contentContainer = new FlowPanel();

    private final SimplePanel mainTabPanelContainer = new SimplePanel();

    private final SimplePanel subTabPanelContainer = new SimplePanel();

    private final SimplePanel overlayPanelContainer = new SimplePanel();

    @Inject
    public MainContentView(final ClientStorage clientStorage) {
        initWidget(contentContainer);
        contentContainer.add(mainTabPanelContainer);
        contentContainer.add(subTabPanelContainer);
        contentContainer.add(overlayPanelContainer);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainContentPresenter.TYPE_SetMainTabPanelContent) {
            setPanelContent(mainTabPanelContainer, content);
        } else if (slot == MainContentPresenter.TYPE_SetSubTabPanelContent) {
            setPanelContent(subTabPanelContainer, content);
        } else if (slot == MainContentPresenter.TYPE_SetOverlayContent) {
            setPanelContent(overlayPanelContainer, content);
        } else {
            super.setInSlot(slot, content);
        }

    }
}
