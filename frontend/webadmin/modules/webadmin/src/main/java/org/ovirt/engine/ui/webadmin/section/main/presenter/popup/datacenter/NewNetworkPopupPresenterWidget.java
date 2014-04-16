package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NewNetworkModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class NewNetworkPopupPresenterWidget extends AbstractNetworkPopupPresenterWidget<NewNetworkModel, NewNetworkPopupPresenterWidget.ViewDef>{

    public interface ViewDef extends AbstractNetworkPopupPresenterWidget.ViewDef<NewNetworkModel> {
        void setNetworkClusterList(ListModel<NetworkClusterModel> networkClusterList);
    }

    @Inject
    public NewNetworkPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    public void init(NewNetworkModel model) {
        super.init(model);
        getView().setNetworkClusterList(model.getNetworkClusterList());
    }

}
