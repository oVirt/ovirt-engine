package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabEventView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;

public class SubTabUserEventView extends AbstractSubTabEventView<DbUser, UserListModel, UserEventListModel>
        implements SubTabUserEventPresenter.ViewDef {

    @Inject
    public SubTabUserEventView(SearchableDetailModelProvider<AuditLog, UserListModel, UserEventListModel> modelProvider) {
        super(modelProvider);
        initWidget(getTable());
    }

}
