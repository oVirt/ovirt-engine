package org.ovirt.engine.ui.common.view;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.NetworkBreadCrumbsPresenterWidget.NetworkBreadCrumbsViewDef;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.MenuDetailsProvider;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class NetworkBreadCrumbsView extends OvirtBreadCrumbsView<NetworkView, NetworkListModel>
    implements NetworkBreadCrumbsViewDef {

    @Inject
    public NetworkBreadCrumbsView(MenuDetailsProvider menu,
            MainModelProvider<NetworkView, NetworkListModel> listModelProvider) {
        super(listModelProvider, menu);
    }

    @Override
    public SafeHtml getName(NetworkView item) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(super.getName(item));
        builder.appendEscaped(" ("); // $NON-NLS-1$
        builder.appendEscaped(item.getDataCenterName());
        builder.appendEscaped(")"); // $NON-NLS-1$
        return builder.toSafeHtml();
    }

}
