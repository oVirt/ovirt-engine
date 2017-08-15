package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;

import com.google.web.bindery.event.shared.EventBus;

public class NetworkBreadCrumbsPresenterWidget extends OvirtBreadCrumbsPresenterWidget<NetworkView, NetworkListModel> {

    public interface NetworkBreadCrumbsViewDef extends OvirtBreadCrumbsPresenterWidget.ViewDef<NetworkView> {
    }

    @Inject
    public NetworkBreadCrumbsPresenterWidget(EventBus eventBus, NetworkBreadCrumbsViewDef view,
            MainModelProvider<NetworkView, NetworkListModel> listModelProvider) {
        super(eventBus, view, listModelProvider);
    }

}
