package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageRegisterTemplateListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class StorageRegisterTemplateActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<StorageDomain, VmTemplate, StorageListModel, StorageRegisterTemplateListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public StorageRegisterTemplateActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<StorageDomain, VmTemplate> view,
            SearchableDetailModelProvider<VmTemplate, StorageListModel,
                StorageRegisterTemplateListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<StorageDomain, VmTemplate>(constants.restoreVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getImportCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<StorageDomain, VmTemplate>(constants.removeTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
