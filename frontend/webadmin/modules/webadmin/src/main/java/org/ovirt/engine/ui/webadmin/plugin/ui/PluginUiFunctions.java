package org.ovirt.engine.ui.webadmin.plugin.ui;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.plugin.entity.BaseEntity;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityType;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunctionResultHelper;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabCustomProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Contains UI related functionality exposed to UI plugins through the plugin API.
 */
public class PluginUiFunctions {

    private final MainTabCustomProxyFactory mainTabCustomProxyFactory;

    private final Provider<MainTabDataCenterPresenter> mainTabDataCenterPresenterProvider;
    private final Provider<MainTabClusterPresenter> mainTabClusterPresenterProvider;
    private final Provider<MainTabHostPresenter> mainTabHostPresenterProvider;
    private final Provider<MainTabStoragePresenter> mainTabStoragePresenterProvider;
    private final Provider<MainTabDiskPresenter> mainTabDiskPresenterProvider;
    private final Provider<MainTabVirtualMachinePresenter> mainTabVirtualMachinePresenterProvider;
    private final Provider<MainTabTemplatePresenter> mainTabTemplatePresenterProvider;

    @Inject
    public PluginUiFunctions(MainTabCustomProxyFactory mainTabCustomProxyFactory,
            Provider<MainTabDataCenterPresenter> mainTabDataCenterPresenterProvider,
            Provider<MainTabClusterPresenter> mainTabClusterPresenterProvider,
            Provider<MainTabHostPresenter> mainTabHostPresenterProvider,
            Provider<MainTabStoragePresenter> mainTabStoragePresenterProvider,
            Provider<MainTabDiskPresenter> mainTabDiskPresenterProvider,
            Provider<MainTabVirtualMachinePresenter> mainTabVirtualMachinePresenterProvider,
            Provider<MainTabTemplatePresenter> mainTabTemplatePresenterProvider) {
        this.mainTabCustomProxyFactory = mainTabCustomProxyFactory;
        this.mainTabDataCenterPresenterProvider = mainTabDataCenterPresenterProvider;
        this.mainTabClusterPresenterProvider = mainTabClusterPresenterProvider;
        this.mainTabHostPresenterProvider = mainTabHostPresenterProvider;
        this.mainTabStoragePresenterProvider = mainTabStoragePresenterProvider;
        this.mainTabDiskPresenterProvider = mainTabDiskPresenterProvider;
        this.mainTabVirtualMachinePresenterProvider = mainTabVirtualMachinePresenterProvider;
        this.mainTabTemplatePresenterProvider = mainTabTemplatePresenterProvider;
    }

    /**
     * Adds new main tab that shows contents of the given URL.
     */
    public void addMainTab(String label, String historyToken, String contentUrl) {
        mainTabCustomProxyFactory.create(label, historyToken, contentUrl);
    }

    /**
     * Adds new main tab action button.
     */
    public void addMainTabActionButton(EntityType entityType, String label, ActionButtonInterface actionButtonInterface) {
        HasActionTable<? extends BusinessEntity<? extends NGuid>> hasTable = null;

        switch (entityType) {
        case DataCenter:
            hasTable = mainTabDataCenterPresenterProvider.get().getView();
            break;
        case Cluster:
            hasTable = mainTabClusterPresenterProvider.get().getView();
            break;
        case Host:
            hasTable = mainTabHostPresenterProvider.get().getView();
            break;
        case Storage:
            hasTable = mainTabStoragePresenterProvider.get().getView();
            break;
        case Disk:
            hasTable = mainTabDiskPresenterProvider.get().getView();
            break;
        case VirtualMachine:
            hasTable = mainTabVirtualMachinePresenterProvider.get().getView();
            break;
        case Template:
            hasTable = mainTabTemplatePresenterProvider.get().getView();
            break;
        }

        ActionTable<? extends BusinessEntity<? extends NGuid>> table = (hasTable != null) ? hasTable.getTable() : null;
        if (table != null) {
            addActionButton(label, actionButtonInterface, table);
        }
    }

    <T extends BusinessEntity<? extends NGuid>> void addActionButton(String label,
            final ActionButtonInterface actionButtonInterface, final ActionTable<T> table) {
        final UICommand command = new UICommand(label, new BaseCommandTarget() {
            @Override
            public void ExecuteCommand(UICommand uiCommand) {
                actionButtonInterface.onClick().invoke(BaseEntity.arrayFrom(table.getSelectedItems()), null);
            }
        });

        table.addActionButton(new WebAdminButtonDefinition<T>(label) {
            @Override
            protected UICommand resolveCommand() {
                return command;
            }
        });

        table.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updateActionButtonCommand(command, actionButtonInterface, table.getSelectedItems());
            }
        });
        updateActionButtonCommand(command, actionButtonInterface, table.getSelectedItems());
    }

    <T extends BusinessEntity<? extends NGuid>> void updateActionButtonCommand(UICommand command,
            ActionButtonInterface actionButtonInterface, List<T> selectedItems) {
        // Update 'IsExecutionAllowed' property
        boolean isEnabled = true;
        isEnabled = JsFunctionResultHelper.invokeAndGetResultAsBoolean(actionButtonInterface.isEnabled(),
                BaseEntity.arrayFrom(selectedItems), null, isEnabled);
        command.setIsExecutionAllowed(isEnabled);

        // Update 'IsAvailable' property
        boolean isAccessible = true;
        isAccessible = JsFunctionResultHelper.invokeAndGetResultAsBoolean(actionButtonInterface.isAccessible(),
                BaseEntity.arrayFrom(selectedItems), null, isAccessible);
        command.setIsAvailable(isAccessible);
    }

}
