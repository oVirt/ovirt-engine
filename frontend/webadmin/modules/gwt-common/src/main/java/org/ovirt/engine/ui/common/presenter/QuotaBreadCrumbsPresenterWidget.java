package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;

import com.google.web.bindery.event.shared.EventBus;

public class QuotaBreadCrumbsPresenterWidget extends OvirtBreadCrumbsPresenterWidget<Quota, QuotaListModel> {

    public interface QuotaBreadCrumbsViewDef extends OvirtBreadCrumbsPresenterWidget.ViewDef<Quota> {
    }

    @Inject
    public QuotaBreadCrumbsPresenterWidget(EventBus eventBus, QuotaBreadCrumbsViewDef view,
            MainModelProvider<Quota, QuotaListModel> listModelProvider) {
        super(eventBus, view, listModelProvider);
    }

}
