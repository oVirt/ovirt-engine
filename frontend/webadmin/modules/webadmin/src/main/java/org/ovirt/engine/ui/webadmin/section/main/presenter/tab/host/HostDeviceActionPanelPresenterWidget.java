package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModel;

import com.google.web.bindery.event.shared.EventBus;

public class HostDeviceActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VDS, HostDeviceView, HostListModel<Void>, HostDeviceListModel> {

    @Inject
    public HostDeviceActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VDS, HostDeviceView> view,
            SearchableDetailModelProvider<HostDeviceView, HostListModel<Void>, HostDeviceListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        // no GWT action buttons
    }
}
