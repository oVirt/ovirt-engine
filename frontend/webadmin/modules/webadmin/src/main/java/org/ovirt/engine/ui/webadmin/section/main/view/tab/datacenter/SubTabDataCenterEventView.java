package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

public class SubTabDataCenterEventView extends AbstractSubTabEventView<storage_pool, DataCenterListModel, DataCenterEventListModel>
        implements SubTabDataCenterEventPresenter.ViewDef {

    @Inject
    public SubTabDataCenterEventView(SearchableDetailModelProvider<AuditLog, DataCenterListModel, DataCenterEventListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
