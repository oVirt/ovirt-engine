package org.ovirt.engine.ui.webadmin.plugin;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;
import org.ovirt.engine.ui.webadmin.plugin.entity.BaseEntity;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsArrayHelper;
import org.ovirt.engine.ui.webadmin.plugin.restapi.RestApiSessionAcquiredEvent;
import org.ovirt.engine.ui.webadmin.plugin.restapi.RestApiSessionAcquiredEvent.RestApiSessionAcquiredHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ClusterSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ClusterSelectionChangeEvent.ClusterSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent.DataCenterSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DiskSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DiskSelectionChangeEvent.DiskSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent.HostSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.StorageSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.StorageSelectionChangeEvent.StorageSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateSelectionChangeEvent.TemplateSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineSelectionChangeEvent.VirtualMachineSelectionChangeHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Handles WebAdmin application events to be consumed by UI plugins.
 * <p>
 * Should be bound as GIN eager singleton, created early on during application startup.
 */
public class PluginEventHandler {

    @Inject
    public PluginEventHandler(EventBus eventBus, final PluginManager manager, final CurrentUser user) {
        // User login and logout
        eventBus.addHandler(UserLoginChangeEvent.getType(), new UserLoginChangeHandler() {
            @Override
            public void onUserLoginChange(UserLoginChangeEvent event) {
                if (user.isLoggedIn()) {
                    manager.invokePluginsNowOrLater("UserLogin", //$NON-NLS-1$
                            JsArrayHelper.createStringArray(user.getUserName(), user.getUserId()));
                } else {
                    manager.invokePluginsNowOrLater("UserLogout", null); //$NON-NLS-1$
                }
            }
        });

        // Engine REST API session management
        eventBus.addHandler(RestApiSessionAcquiredEvent.getType(), new RestApiSessionAcquiredHandler() {
            @Override
            public void onRestApiSessionAcquired(RestApiSessionAcquiredEvent event) {
                manager.invokePluginsNowOrLater("RestApiSessionAcquired", //$NON-NLS-1$
                        JsArrayHelper.createStringArray(event.getSessionId()));
            }
        });

        // Standard main tab item selection handling
        eventBus.addHandler(ClusterSelectionChangeEvent.getType(), new ClusterSelectionChangeHandler() {
            @Override
            public void onClusterSelectionChange(ClusterSelectionChangeEvent event) {
                manager.invokePluginsNow("ClusterSelectionChange", BaseEntity.arrayFrom(event.getSelectedItems())); //$NON-NLS-1$
            }
        });
        eventBus.addHandler(DataCenterSelectionChangeEvent.getType(), new DataCenterSelectionChangeHandler() {
            @Override
            public void onDataCenterSelectionChange(DataCenterSelectionChangeEvent event) {
                manager.invokePluginsNow("DataCenterSelectionChange", BaseEntity.arrayFrom(event.getSelectedItems())); //$NON-NLS-1$
            }
        });
        eventBus.addHandler(DiskSelectionChangeEvent.getType(), new DiskSelectionChangeHandler() {
            @Override
            public void onDiskSelectionChange(DiskSelectionChangeEvent event) {
                manager.invokePluginsNow("DiskSelectionChange", BaseEntity.arrayFrom(event.getSelectedItems())); //$NON-NLS-1$
            }
        });
        eventBus.addHandler(HostSelectionChangeEvent.getType(), new HostSelectionChangeHandler() {
            @Override
            public void onHostSelectionChange(HostSelectionChangeEvent event) {
                manager.invokePluginsNow("HostSelectionChange", BaseEntity.arrayFrom(event.getSelectedItems())); //$NON-NLS-1$
            }
        });
        eventBus.addHandler(StorageSelectionChangeEvent.getType(), new StorageSelectionChangeHandler() {
            @Override
            public void onStorageSelectionChange(StorageSelectionChangeEvent event) {
                manager.invokePluginsNow("StorageSelectionChange", BaseEntity.arrayFrom(event.getSelectedItems())); //$NON-NLS-1$
            }
        });
        eventBus.addHandler(TemplateSelectionChangeEvent.getType(), new TemplateSelectionChangeHandler() {
            @Override
            public void onTemplateSelectionChange(TemplateSelectionChangeEvent event) {
                manager.invokePluginsNow("TemplateSelectionChange", BaseEntity.arrayFrom(event.getSelectedItems())); //$NON-NLS-1$
            }
        });
        eventBus.addHandler(VirtualMachineSelectionChangeEvent.getType(), new VirtualMachineSelectionChangeHandler() {
            @Override
            public void onVirtualMachineSelectionChange(VirtualMachineSelectionChangeEvent event) {
                manager.invokePluginsNow("VirtualMachineSelectionChange", BaseEntity.arrayFrom(event.getSelectedItems())); //$NON-NLS-1$
            }
        });
    }

}
