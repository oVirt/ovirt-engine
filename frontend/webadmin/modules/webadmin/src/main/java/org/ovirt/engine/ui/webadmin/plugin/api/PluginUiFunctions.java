package org.ovirt.engine.ui.webadmin.plugin.api;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.common.presenter.RedrawDynamicTabContainerEvent;
import org.ovirt.engine.ui.common.presenter.SetDynamicTabAccessibleEvent;
import org.ovirt.engine.ui.common.widget.table.ActionTable;
import org.ovirt.engine.ui.common.widget.table.HasActionTable;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityObject;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityType;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunctionResultHelper;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabContentUrlEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CloseDynamicPopupEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DynamicUrlContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.SetDynamicPopupContentUrlEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.DiskSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.StorageSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VirtualMachineSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

/**
 * Contains UI related functionality exposed to UI plugins through the plugin API.
 */
public class PluginUiFunctions implements HasHandlers {

    private final EventBus eventBus;
    private final DynamicUrlContentTabProxyFactory dynamicUrlContentTabProxyFactory;

    private final Provider<MainTabDataCenterPresenter> mainTabDataCenterPresenterProvider;
    private final Provider<MainTabClusterPresenter> mainTabClusterPresenterProvider;
    private final Provider<MainTabHostPresenter> mainTabHostPresenterProvider;
    private final Provider<MainTabStoragePresenter> mainTabStoragePresenterProvider;
    private final Provider<MainTabDiskPresenter> mainTabDiskPresenterProvider;
    private final Provider<MainTabVirtualMachinePresenter> mainTabVirtualMachinePresenterProvider;
    private final Provider<MainTabTemplatePresenter> mainTabTemplatePresenterProvider;

    private final Provider<DynamicUrlContentPopupPresenterWidget> dynamicUrlContentPopupPresenterWidgetProvider;

    @Inject
    public PluginUiFunctions(EventBus eventBus,
            DynamicUrlContentTabProxyFactory dynamicUrlContentTabProxyFactory,
            Provider<MainTabDataCenterPresenter> mainTabDataCenterPresenterProvider,
            Provider<MainTabClusterPresenter> mainTabClusterPresenterProvider,
            Provider<MainTabHostPresenter> mainTabHostPresenterProvider,
            Provider<MainTabStoragePresenter> mainTabStoragePresenterProvider,
            Provider<MainTabDiskPresenter> mainTabDiskPresenterProvider,
            Provider<MainTabVirtualMachinePresenter> mainTabVirtualMachinePresenterProvider,
            Provider<MainTabTemplatePresenter> mainTabTemplatePresenterProvider,
            Provider<DynamicUrlContentPopupPresenterWidget> dynamicUrlContentPopupPresenterWidgetProvider) {
        this.eventBus = eventBus;
        this.dynamicUrlContentTabProxyFactory = dynamicUrlContentTabProxyFactory;
        this.mainTabDataCenterPresenterProvider = mainTabDataCenterPresenterProvider;
        this.mainTabClusterPresenterProvider = mainTabClusterPresenterProvider;
        this.mainTabHostPresenterProvider = mainTabHostPresenterProvider;
        this.mainTabStoragePresenterProvider = mainTabStoragePresenterProvider;
        this.mainTabDiskPresenterProvider = mainTabDiskPresenterProvider;
        this.mainTabVirtualMachinePresenterProvider = mainTabVirtualMachinePresenterProvider;
        this.mainTabTemplatePresenterProvider = mainTabTemplatePresenterProvider;
        this.dynamicUrlContentPopupPresenterWidgetProvider = dynamicUrlContentPopupPresenterWidgetProvider;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    /**
     * Adds new dynamic main tab that shows contents of the given URL.
     */
    public void addMainTab(String label, String historyToken, String contentUrl) {
        addTab(MainTabPanelPresenter.TYPE_RequestTabs,
                MainTabPanelPresenter.TYPE_SetTabContent,
                label, historyToken, true, contentUrl);
    }

    /**
     * Adds new dynamic sub tab that shows contents of the given URL.
     */
    public void addSubTab(EntityType entityType, String label, String historyToken, String contentUrl) {
        switch (entityType) {
        case DataCenter:
            addTab(DataCenterSubTabPanelPresenter.TYPE_RequestTabs,
                    DataCenterSubTabPanelPresenter.TYPE_SetTabContent,
                    label, historyToken, false, contentUrl);
            break;
        case Cluster:
            addTab(ClusterSubTabPanelPresenter.TYPE_RequestTabs,
                    ClusterSubTabPanelPresenter.TYPE_SetTabContent,
                    label, historyToken, false, contentUrl);
            break;
        case Host:
            addTab(HostSubTabPanelPresenter.TYPE_RequestTabs,
                    HostSubTabPanelPresenter.TYPE_SetTabContent,
                    label, historyToken, false, contentUrl);
            break;
        case Storage:
            addTab(StorageSubTabPanelPresenter.TYPE_RequestTabs,
                    StorageSubTabPanelPresenter.TYPE_SetTabContent,
                    label, historyToken, false, contentUrl);
            break;
        case Disk:
            addTab(DiskSubTabPanelPresenter.TYPE_RequestTabs,
                    DiskSubTabPanelPresenter.TYPE_SetTabContent,
                    label, historyToken, false, contentUrl);
            break;
        case VirtualMachine:
            addTab(VirtualMachineSubTabPanelPresenter.TYPE_RequestTabs,
                    VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent,
                    label, historyToken, false, contentUrl);
            break;
        case Template:
            addTab(TemplateSubTabPanelPresenter.TYPE_RequestTabs,
                    TemplateSubTabPanelPresenter.TYPE_SetTabContent,
                    label, historyToken, false, contentUrl);
            break;
        }
    }

    void addTab(Type<RequestTabsHandler> requestTabsEventType,
            Type<RevealContentHandler<?>> revealContentEventType,
            String label, String historyToken,
            boolean isMainTab, String contentUrl) {
        // Create and bind tab presenter proxy
        dynamicUrlContentTabProxyFactory.create(
                requestTabsEventType, revealContentEventType,
                label, historyToken, isMainTab, contentUrl);

        // Redraw the corresponding tab container
        RedrawDynamicTabContainerEvent.fire(this, requestTabsEventType);
    }

    /**
     * Sets the content URL for existing dynamic tab.
     */
    public void setTabContentUrl(final String historyToken, final String contentUrl) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                SetDynamicTabContentUrlEvent.fire(PluginUiFunctions.this, historyToken, contentUrl);
            }
        });
    }

    /**
     * Updates tab/place accessibility for existing dynamic tab.
     */
    public void setTabAccessible(final String historyToken, final boolean tabAccessible) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                SetDynamicTabAccessibleEvent.fire(PluginUiFunctions.this, historyToken, tabAccessible);
            }
        });
    }

    /**
     * Adds new action button to a standard table-based main tab.
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
                actionButtonInterface.onClick().invoke(EntityObject.arrayFrom(table.getSelectedItems()), null);
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
                EntityObject.arrayFrom(selectedItems), null, isEnabled);
        command.setIsExecutionAllowed(isEnabled);

        // Update 'IsAvailable' property
        boolean isAccessible = true;
        isAccessible = JsFunctionResultHelper.invokeAndGetResultAsBoolean(actionButtonInterface.isAccessible(),
                EntityObject.arrayFrom(selectedItems), null, isAccessible);
        command.setIsAvailable(isAccessible);
    }

    /**
     * Shows a modal dialog with content loaded from the given URL.
     */
    public void showDialog(String title, String dialogToken, String contentUrl, String width, String height,
            DialogOptions options) {
        DynamicUrlContentPopupPresenterWidget popup = dynamicUrlContentPopupPresenterWidgetProvider.get();
        popup.init(dialogToken, title, width, height,
                options.getResizeEnabled(), options.getCloseIconVisible(), options.getCloseOnEscKey());
        popup.setContentUrl(contentUrl);

        // Add dialog buttons
        JsArray<DialogButtonInterface> buttons = options.getButtons();
        for (int i = 0; i < buttons.length(); i++) {
            final DialogButtonInterface dialogButtonInterface = buttons.get(i);

            popup.addFooterButton(dialogButtonInterface.getLabel(), new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    dialogButtonInterface.onClick().invoke(null, null);
                }
            });
        }

        // Reveal the popup
        RevealRootPopupContentEvent.fire(this, popup);
    }

    /**
     * Sets the content URL for existing modal dialog.
     */
    public void setDialogContentUrl(final String dialogToken, final String contentUrl) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                SetDynamicPopupContentUrlEvent.fire(PluginUiFunctions.this, dialogToken, contentUrl);
            }
        });
    }

    /**
     * Closes an existing modal dialog.
     */
    public void closeDialog(final String dialogToken) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                CloseDynamicPopupEvent.fire(PluginUiFunctions.this, dialogToken);
            }
        });
    }

}
