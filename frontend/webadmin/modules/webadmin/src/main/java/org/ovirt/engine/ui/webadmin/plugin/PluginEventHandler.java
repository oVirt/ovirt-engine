package org.ovirt.engine.ui.webadmin.plugin;

import java.util.logging.Logger;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.auth.UserLoginChangeEvent;
import org.ovirt.engine.ui.common.presenter.DetailItemSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityObject;
import org.ovirt.engine.ui.webadmin.plugin.entity.TagObject;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsArrayHelper;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ClusterSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DataCenterSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DiskSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.EventSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HostSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.NetworkSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.PoolSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.ProviderSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.QuotaSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.StorageSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.TemplateSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.UserSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.VirtualMachineSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.VolumeSelectionChangeEvent;
import org.ovirt.engine.ui.webadmin.system.MessageEventData;
import org.ovirt.engine.ui.webadmin.system.MessageReceivedEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagActivationChangeEvent;

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
        eventBus.addHandler(UserLoginChangeEvent.getType(), event -> {
            if (user.isLoggedIn()) {
                manager.invokePluginsNowOrLater("UserLogin", //$NON-NLS-1$
                        JsArrayHelper.createStringArray(user.getFullUserName(), user.getUserId()));
            } else {
                manager.invokePluginsNowOrLater("UserLogout", null); //$NON-NLS-1$
            }
        });

        // Main place item selection change
        eventBus.addHandler(DataCenterSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("DataCenterSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(ClusterSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("ClusterSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(HostSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("HostSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(NetworkSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("NetworkSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(StorageSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("StorageSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(DiskSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("DiskSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(VirtualMachineSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("VirtualMachineSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(PoolSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("PoolSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(TemplateSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("TemplateSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(VolumeSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("GlusterVolumeSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(ProviderSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("ProviderSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(UserSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("UserSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(QuotaSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("QuotaSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));
        eventBus.addHandler(EventSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("EventSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));

        // Detail place item selection change
        // Note: the 'DetailItemSelectionChange' event covers all detail tabs.
        // To determine which detail tab the user is currently on, use the 'currentPlace' API function.
        eventBus.addHandler(DetailItemSelectionChangeEvent.getType(),
                event -> manager.invokePluginsNow("DetailItemSelectionChange", //$NON-NLS-1$
                        JsArrayHelper.createMixedArray(EntityObject.arrayFrom(event.getSelectedItems()))));

        // Cross-window messaging
        eventBus.addHandler(MessageReceivedEvent.getType(), event -> {
            final MessageEventData eventData = event.getData();

            manager.invokePluginsNowOrLater("MessageReceived", //$NON-NLS-1$
                    JsArrayHelper.createMixedArray(eventData.getData(), eventData.getSourceWindow()),
                    plugin -> {
                        if (eventData.originMatches(plugin.getApiOptionsObject().getAllowedMessageOrigins())) {
                            return true;
                        }

                        logger.info("Plugin [" + plugin.getName() //$NON-NLS-1$
                                + "] rejected message event for origin [" + eventData.getOrigin() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
                        return false;
                    });
        });

        // Tag Activation Change Event.
        eventBus.addHandler(TagActivationChangeEvent.getType(),
                event -> manager.invokePluginsNow("TagActivationChange", //$NON-NLS-1$
                        TagObject.activeTagArray(event.getActiveTags())));
    }

}
