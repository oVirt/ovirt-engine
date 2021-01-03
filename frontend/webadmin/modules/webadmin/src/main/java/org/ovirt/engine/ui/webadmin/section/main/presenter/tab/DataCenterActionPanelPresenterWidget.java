package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class DataCenterActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, StoragePool, DataCenterListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, StoragePool> newButtonDefinition;

    @Inject
    public DataCenterActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, StoragePool> view,
            MainModelProvider<StoragePool, DataCenterListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, StoragePool>(constants.newDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);

        addActionButton(new WebAdminButtonDefinition<Void, StoragePool>(constants.editDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, StoragePool>(constants.removeDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, StoragePool>(constants.forceRemoveDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getForceRemoveCommand();
            }
        });

        addMenuListItem(new WebAdminImageButtonDefinition<Void, StoragePool>(constants.guideMeDc(), IconType.SUPPORT, true) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getGuideCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, StoragePool>(constants.reinitializeDC()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRecoveryStorageCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, StoragePool>(constants.cleanupFinishedTasks()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCleanupFinishedTasksCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, StoragePool> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
