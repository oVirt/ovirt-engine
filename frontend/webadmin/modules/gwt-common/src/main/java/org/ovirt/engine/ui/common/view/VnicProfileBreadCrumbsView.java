package org.ovirt.engine.ui.common.view;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.VnicProfileBreadCrumbsPresenterWidget.VnicProfileBreadCrumbsViewDef;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.MenuDetailsProvider;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class VnicProfileBreadCrumbsView extends OvirtBreadCrumbsView<VnicProfileView, VnicProfileListModel>
    implements VnicProfileBreadCrumbsViewDef {

    @Inject
    public VnicProfileBreadCrumbsView(MenuDetailsProvider menu,
            MainModelProvider<VnicProfileView, VnicProfileListModel> listModelProvider) {
        super(listModelProvider, menu);
    }

    @Override
    public SafeHtml getName(VnicProfileView item) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(super.getName(item));
        builder.appendEscaped(" ("); // $NON-NLS-1$
        builder.appendEscaped(item.getNetworkName());
        builder.appendEscaped("/"); // $NON-NLS-1$
        builder.appendEscaped(item.getDataCenterName());
        builder.appendEscaped(")"); // $NON-NLS-1$
        return builder.toSafeHtml();
    }
}
