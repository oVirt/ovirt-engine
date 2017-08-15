package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;

import com.google.web.bindery.event.shared.EventBus;

public class VnicProfileBreadCrumbsPresenterWidget
    extends OvirtBreadCrumbsPresenterWidget<VnicProfileView, VnicProfileListModel> {

    public interface VnicProfileBreadCrumbsViewDef extends OvirtBreadCrumbsPresenterWidget.ViewDef<VnicProfileView> {
    }

    @Inject
    public VnicProfileBreadCrumbsPresenterWidget(EventBus eventBus, VnicProfileBreadCrumbsViewDef view,
            MainModelProvider<VnicProfileView, VnicProfileListModel> listModelProvider) {
        super(eventBus, view, listModelProvider);
    }

}
