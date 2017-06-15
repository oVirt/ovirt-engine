package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SubTabQuotaEventView extends AbstractSubTabEventView<Quota, QuotaListModel, QuotaEventListModel>
        implements SubTabQuotaEventPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaEventView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabQuotaEventView(SearchableDetailModelProvider<AuditLog, QuotaListModel, QuotaEventListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
