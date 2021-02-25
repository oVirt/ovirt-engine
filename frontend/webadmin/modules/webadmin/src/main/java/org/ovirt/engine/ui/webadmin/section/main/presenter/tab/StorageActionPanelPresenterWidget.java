package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class StorageActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, StorageDomain, StorageListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, StorageDomain> newButtonDefinition;
    private WebAdminButtonDefinition<Void, StorageDomain> importButtonDefinition;

    @Inject
    public StorageActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, StorageDomain> view,
            MainModelProvider<StorageDomain, StorageListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, StorageDomain>(constants.newDomainStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewDomainCommand();
            }
        };
        addActionButton(newButtonDefinition);
        importButtonDefinition = new WebAdminButtonDefinition<Void, StorageDomain>(constants.importDomainStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getImportDomainCommand();
            }
        };
        addActionButton(importButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<Void, StorageDomain>(constants.editStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, StorageDomain>(constants.removeStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, StorageDomain>(constants.updateOvfsForStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUpdateOvfsCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, StorageDomain>(constants.destroyStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getDestroyCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, StorageDomain>(constants.scanDisksStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getScanDisksCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<Void, StorageDomain>(constants.selectStorageDomainAsMaster()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getSwitchMasterCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, StorageDomain> getNewButtonDefinition() {
        return newButtonDefinition;
    }

    public WebAdminButtonDefinition<Void, StorageDomain> getImportButtonDefinition() {
        return importButtonDefinition;
    }
}
