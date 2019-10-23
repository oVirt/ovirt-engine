package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterQuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class DataCenterQuotaActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StoragePool, Quota, DataCenterListModel, DataCenterQuotaListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DataCenterQuotaActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StoragePool, Quota> view,
            SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StoragePool, Quota>(constants.addQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCreateCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, Quota>(constants.editQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, Quota>(constants.copyQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCloneCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StoragePool, Quota>(constants.removeQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
