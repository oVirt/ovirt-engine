package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaUserListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class QuotaUserActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<Quota, Permission, QuotaListModel, QuotaUserListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public QuotaUserActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Quota, Permission> view,
            SearchableDetailModelProvider<Permission, QuotaListModel, QuotaUserListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Quota, Permission>(constants.addUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getAddCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Quota, Permission>(constants.removeUser()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }
}
