package org.ovirt.engine.ui.webadmin.plugin;

import java.util.logging.Logger;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent.UserLoginChangeHandler;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager.PluginInvocationCondition;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityObject;
import org.ovirt.engine.ui.webadmin.plugin.entity.SystemTreeItemObject;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsArrayHelper;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ClusterSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ClusterSelectionChangeEvent.ClusterSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DataCenterSelectionChangeEvent.DataCenterSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DiskSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DiskSelectionChangeEvent.DiskSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.EventSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.EventSelectionChangeEvent.EventSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent.HostSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.NetworkSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.NetworkSelectionChangeEvent.NetworkSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.PoolSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.PoolSelectionChangeEvent.PoolSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ProviderSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.ProviderSelectionChangeEvent.ProviderSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.QuotaSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.QuotaSelectionChangeEvent.QuotaSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.StorageSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.StorageSelectionChangeEvent.StorageSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateSelectionChangeEvent.TemplateSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.UserSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.UserSelectionChangeEvent.UserSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VirtualMachineSelectionChangeEvent.VirtualMachineSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VolumeSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.VolumeSelectionChangeEvent.VolumeSelectionChangeHandler;
import org.ovirt.engine.ui.webadmin.system.MessageEventData;
import org.ovirt.engine.ui.webadmin.system.MessageReceivedEvent;
import org.ovirt.engine.ui.webadmin.system.MessageReceivedEvent.MessageReceivedHandler;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemTreeSelectionChangeEvent.SystemTreeSelectionChangeHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Handles WebAdmin application events to be consumed by UI plugins.
 * <p>
 * Should be bound as GIN eager singleton, created early on during application startup.
 */
public class PluginEventHandler {

    private static final Logger logger = Logger.getLogger(PluginEventHandler.class.getName());

    @Inject
    public PluginEventHandler(EventBus eventBus, final PluginManager manager, final CurrentUser user) {
        // User login and logout
        eventBus.addHandler(UserLoginChangeEvent.getType(), new UserLoginChangeHandler() {
            @Override
            public void onUserLoginChange(UserLoginChangeEvent event) {
                if (user.isLoggedIn()) {
                    manager.invokePluginsNowOrLater("UserLogin", //$NON-NLS-1$
                            JsArrayHelper.createStringArray(user.getFullUserName(), user.getUserId()));
                } else {
                    manager.invokePluginsNowOrLater("UserLogout", null); //$NON-NLS-1$
                }
            }
        });

        // Standard main tab item selection change
        eventBus.addHandler(DataCenterSelectionChangeEvent.getType(), new DataCenterSelectionChangeHandler() {
            @Override
            public void onDataCenterSelectionChange(DataCenterSelectionChangeEvent event) {
                manager.invokePluginsNow("DataCenterSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(ClusterSelectionChangeEvent.getType(), new ClusterSelectionChangeHandler() {
            @Override
            public void onClusterSelectionChange(ClusterSelectionChangeEvent event) {
                manager.invokePluginsNow("ClusterSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(HostSelectionChangeEvent.getType(), new HostSelectionChangeHandler() {
            @Override
            public void onHostSelectionChange(HostSelectionChangeEvent event) {
                manager.invokePluginsNow("HostSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(NetworkSelectionChangeEvent.getType(), new NetworkSelectionChangeHandler() {
            @Override
            public void onNetworkSelectionChange(NetworkSelectionChangeEvent event) {
                manager.invokePluginsNow("NetworkSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(StorageSelectionChangeEvent.getType(), new StorageSelectionChangeHandler() {
            @Override
            public void onStorageSelectionChange(StorageSelectionChangeEvent event) {
                manager.invokePluginsNow("StorageSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(DiskSelectionChangeEvent.getType(), new DiskSelectionChangeHandler() {
            @Override
            public void onDiskSelectionChange(DiskSelectionChangeEvent event) {
                manager.invokePluginsNow("DiskSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(VirtualMachineSelectionChangeEvent.getType(), new VirtualMachineSelectionChangeHandler() {
            @Override
            public void onVirtualMachineSelectionChange(VirtualMachineSelectionChangeEvent event) {
                manager.invokePluginsNow("VirtualMachineSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(PoolSelectionChangeEvent.getType(), new PoolSelectionChangeHandler() {
            @Override
            public void onPoolSelectionChange(PoolSelectionChangeEvent event) {
                manager.invokePluginsNow("PoolSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(TemplateSelectionChangeEvent.getType(), new TemplateSelectionChangeHandler() {
            @Override
            public void onTemplateSelectionChange(TemplateSelectionChangeEvent event) {
                manager.invokePluginsNow("TemplateSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(VolumeSelectionChangeEvent.getType(), new VolumeSelectionChangeHandler() {
            @Override
            public void onVolumeSelectionChange(VolumeSelectionChangeEvent event) {
                manager.invokePluginsNow("GlusterVolumeSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(ProviderSelectionChangeEvent.getType(), new ProviderSelectionChangeHandler() {
            @Override
            public void onProviderSelectionChange(ProviderSelectionChangeEvent event) {
                manager.invokePluginsNow("ProviderSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(UserSelectionChangeEvent.getType(), new UserSelectionChangeHandler() {
            @Override
            public void onUserSelectionChange(UserSelectionChangeEvent event) {
                manager.invokePluginsNow("UserSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(QuotaSelectionChangeEvent.getType(), new QuotaSelectionChangeHandler() {
            @Override
            public void onQuotaSelectionChange(QuotaSelectionChangeEvent event) {
                manager.invokePluginsNow("QuotaSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });
        eventBus.addHandler(EventSelectionChangeEvent.getType(), new EventSelectionChangeHandler() {
            @Override
            public void onEventSelectionChange(EventSelectionChangeEvent event) {
                manager.invokePluginsNow("EventSelectionChange", //$NON-NLS-1$
                        EntityObject.arrayFrom(event.getSelectedItems()));
            }
        });

        // System tree item selection change
        eventBus.addHandler(SystemTreeSelectionChangeEvent.getType(), new SystemTreeSelectionChangeHandler() {
            @Override
            public void onSystemTreeSelectionChange(SystemTreeSelectionChangeEvent event) {
                if (event.getSelectedItem() != null) {
                    manager.invokePluginsNow("SystemTreeSelectionChange", //$NON-NLS-1$
                            JsArrayHelper.createMixedArray(
                                    SystemTreeItemObject.from(event.getSelectedItem())));
                }
            }
        });

        // Cross-window messaging
        eventBus.addHandler(MessageReceivedEvent.getType(), new MessageReceivedHandler() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
                final MessageEventData eventData = event.getData();

                manager.invokePluginsNowOrLater("MessageReceived", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(eventData.getData(), eventData.getSourceWindow()),
                        new PluginInvocationCondition() {
                            @Override
                            public boolean canInvoke(Plugin plugin) {
                                if (eventData.originMatches(plugin.getApiOptionsObject().getAllowedMessageOrigins())) {
                                    return true;
                                }

                                logger.info("Plugin [" + plugin.getName() //$NON-NLS-1$
                                        + "] rejected message event for origin [" + eventData.getOrigin() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                                return false;
                            }
                        });
            }
        });
    }

}
