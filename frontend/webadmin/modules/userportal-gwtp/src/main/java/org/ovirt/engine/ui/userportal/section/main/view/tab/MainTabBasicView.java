package org.ovirt.engine.ui.userportal.section.main.view.tab;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabBasicPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalBasicListProvider;
import org.ovirt.engine.ui.userportal.widget.refresh.RefreshManager;
import org.ovirt.engine.ui.userportal.widget.refresh.RefreshPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicView extends AbstractView implements MainTabBasicPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    LayoutPanel vmListPanel;

    @UiField
    LayoutPanel vmDetailPanel;

    @UiField(provided = true)
    RefreshPanel refreshPanel;

    private RefreshManager refreshManager;

    @Inject
    public MainTabBasicView(ClientStorage clientStorage, UserPortalBasicListProvider modelProvider) {
        this.refreshManager = new RefreshManager(modelProvider.getModel(), clientStorage);
        this.refreshPanel = refreshManager.getRefreshPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == MainTabBasicPresenter.TYPE_VmListContent) {
            setPanelContent(vmListPanel, content);
        } else if (slot == MainTabBasicPresenter.TYPE_VmDetailsContent) {
            setPanelContent(vmDetailPanel, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public HasClickHandlers getRefreshButton() {
        return refreshPanel;
    }

}
