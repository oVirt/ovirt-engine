package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

public class SubTabHostEventView extends AbstractSubTabEventView<VDS, HostListModel, HostEventListModel>
        implements SubTabHostEventPresenter.ViewDef {

    @Inject
    public SubTabHostEventView(SearchableDetailModelProvider<AuditLog, HostListModel, HostEventListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
