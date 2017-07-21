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

public class StorageActionPanelPresenterWidget extends ActionPanelPresenterWidget<StorageDomain, StorageListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<StorageDomain> newButtonDefinition;
    private WebAdminButtonDefinition<StorageDomain> importButtonDefinition;

    @Inject
    public StorageActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<StorageDomain> view,
            MainModelProvider<StorageDomain, StorageListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<StorageDomain>(constants.newDomainStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewDomainCommand();
            }
        };
        addActionButton(newButtonDefinition);
        importButtonDefinition = new WebAdminButtonDefinition<StorageDomain>(constants.importDomainStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getImportDomainCommand();
            }
        };
        addActionButton(importButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<StorageDomain>(constants.editStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<StorageDomain>(constants.removeStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<StorageDomain>(constants.updateOvfsForStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUpdateOvfsCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<StorageDomain>(constants.destroyStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getDestroyCommand();
            }
        });
        addMenuListItem(new WebAdminButtonDefinition<StorageDomain>(constants.scanDisksStorage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getScanDisksCommand();
            }
        });
    }

    public WebAdminButtonDefinition<StorageDomain> getNewButtonDefinition() {
        return newButtonDefinition;
    }

    public WebAdminButtonDefinition<StorageDomain> getImportButtonDefinition() {
        return importButtonDefinition;
    }
}
