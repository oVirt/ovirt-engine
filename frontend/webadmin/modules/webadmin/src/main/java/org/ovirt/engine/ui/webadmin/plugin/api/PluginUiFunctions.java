package org.ovirt.engine.ui.webadmin.plugin.api;

import java.util.List;

import org.ovirt.engine.ui.common.presenter.AddActionButtonEvent;
import org.ovirt.engine.ui.common.presenter.RedrawDynamicTabContainerEvent;
import org.ovirt.engine.ui.common.presenter.SetDynamicTabAccessibleEvent;
import org.ovirt.engine.ui.common.widget.AlertManager;
import org.ovirt.engine.ui.common.widget.action.AbstractButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification.NotificationStatus;
import org.ovirt.engine.ui.uicommonweb.models.ApplySearchStringEvent;
import org.ovirt.engine.ui.webadmin.place.WebAdminPlaceManager;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityObject;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityType;
import org.ovirt.engine.ui.webadmin.plugin.entity.TagObject;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsArrayHelper;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunctionResultHelper;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MenuPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.NotificationPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabContentUrlEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabUnloadHandlerEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CloseDynamicPopupEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DynamicUrlContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.SetDynamicPopupContentUrlEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Contains UI related functionality exposed to UI plugins through the plugin API.
 */
public class PluginUiFunctions implements HasHandlers {

    private final EventBus eventBus;

    private final DynamicUrlContentTabProxyFactory dynamicUrlContentTabProxyFactory;
    private final DynamicUrlContentProxyFactory dynamicUrlContentProxyFactory;
    private final Provider<DynamicUrlContentPopupPresenterWidget> dynamicUrlContentPopupPresenterWidgetProvider;

    private final TagModelProvider tagModelProvider;
    private final WebAdminPlaceManager placeManager;
    private final AlertManager alertManager;
    private final MenuPresenterWidget menuPresenterWidget;
    private final NotificationPresenterWidget notificationPresenterWidget;

    @Inject
    public PluginUiFunctions(EventBus eventBus,
            DynamicUrlContentTabProxyFactory dynamicUrlContentTabProxyFactory,
            DynamicUrlContentProxyFactory dynamicUrlContentProxyFactory,
            Provider<DynamicUrlContentPopupPresenterWidget> dynamicUrlContentPopupPresenterWidgetProvider,
            WebAdminPlaceManager placeManager,
            AlertManager alertManager,
            MenuPresenterWidget menuPresenterWidget,
            NotificationPresenterWidget notificationPresenterWidget,
            TagModelProvider tagModelProvider) {
        this.eventBus = eventBus;
        this.dynamicUrlContentTabProxyFactory = dynamicUrlContentTabProxyFactory;
        this.dynamicUrlContentProxyFactory = dynamicUrlContentProxyFactory;
        this.dynamicUrlContentPopupPresenterWidgetProvider = dynamicUrlContentPopupPresenterWidgetProvider;
        this.tagModelProvider = tagModelProvider;
        this.placeManager = placeManager;
        this.alertManager = alertManager;
        this.menuPresenterWidget = menuPresenterWidget;
        this.notificationPresenterWidget = notificationPresenterWidget;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    /**
     * Adds new primary menu with a link to the new dynamic main content view. The content view shows the
     * contents of the given URL.
     */
    public void addPrimaryMenuPlace(String label, String historyToken,
            String contentUrl, TabOptions options) {
        addContentView(label, historyToken, contentUrl, null, options.getIcon(), options.getPriority().intValue(),
                options.getDefaultPlace().booleanValue());
    }

    /**
     * Adds new dynamic main content view that shows contents of the given URL.
     */
    public void addPrimaryMenuContainer(String label, String primaryMenuId, TabOptions options) {
        menuPresenterWidget.addPrimaryMenuItemContainer(label, primaryMenuId, options.getPriority().intValue(),
                options.getIcon());
    }

    /**
     * Adds new dynamic secondary content view that shows contents of the given URL, but is visible as a secondary menu
     * to an existing primary menu.
     */
    public void addSecondaryMenu(String primaryMenuId, String label, String historyToken,
            String contentUrl, TabOptions options) {
        addContentView(label, historyToken, contentUrl, primaryMenuId, options.getIcon(),
                options.getPriority().intValue(),
                options.getDefaultPlace().booleanValue());
    }

    /**
     * Adds new dynamic main content view that shows contents of the given URL.
     */
    public void addContentView(String label, String historyToken,
            String contentUrl, String primaryMenuId, String iconCssName, int priority, boolean defaultPlace) {
        menuPresenterWidget.addMenuItemPlace(priority, label, historyToken, primaryMenuId, iconCssName);
        // Not interested in the actual proxy, it will register itself.
        dynamicUrlContentProxyFactory.create(historyToken, contentUrl);
        placeManager.setDefaultPlace(historyToken);
    }

    /**
     * Adds new dynamic sub tab that shows contents of the given URL.
     */
    public void addDetailPlace(EntityType entityType, String label,
            String historyToken, String contentUrl, TabOptions options) {
        Type<RequestTabsHandler> requestTabsEventType = entityType.getSubTabPanelRequestTabs();
        Type<ChangeTabHandler> changeTabEventType = entityType.getSubTabPanelChangeTab();
        Type<RevealContentHandler<?>> slot = entityType.getSubTabPanelContentSlot();

        if (requestTabsEventType != null && changeTabEventType != null && slot != null) {
            addTab(requestTabsEventType, changeTabEventType, slot,
                    label, historyToken, contentUrl, options);
        }
    }

    void addTab(Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            Type<RevealContentHandler<?>> slot,
            String label, String historyToken,
            String contentUrl, TabOptions options) {
        // Create and bind tab presenter proxy
        dynamicUrlContentTabProxyFactory.create(
                requestTabsEventType, changeTabEventType, slot,
                label, options.getPriority().floatValue(),
                historyToken, contentUrl,
                options.getSearchPrefix());

        // Redraw the corresponding tab container
        RedrawDynamicTabContainerEvent.fire(this, requestTabsEventType);
    }

    /**
     * Sets the content URL for existing dynamic tab.
     */
    public void setPlaceContentUrl(final String historyToken, final String contentUrl) {
        Scheduler.get().scheduleDeferred(() -> SetDynamicTabContentUrlEvent.fire(PluginUiFunctions.this,
                historyToken, contentUrl));
    }

    /**
     * Updates tab/place accessibility for existing dynamic tab.
     */
    public void setPlaceAccessible(final String historyToken, final boolean tabAccessible) {
        Scheduler.get().scheduleDeferred(() -> SetDynamicTabAccessibleEvent.fire(PluginUiFunctions.this,
                historyToken, tabAccessible));
    }

    /**
     * Adds tab/place iframe unload handler. This handler is called when the user
     * transitions away from the application place denoted by {@code historyToken}.
     */
    public void setPlaceUnloadHandler(final String historyToken, final JavaScriptObject unloadHandler) {
        Scheduler.get().scheduleDeferred(() -> SetDynamicTabUnloadHandlerEvent.fire(PluginUiFunctions.this,
                historyToken, unloadHandler));
    }

    /**
     * Adds new action button to standard table-based main tab.
     */
    public void addMenuPlaceActionButton(EntityType entityType, String label,
            ActionButtonInterface actionButtonInterface) {
        String historyToken = entityType.getMainHistoryToken();

        if (historyToken != null) {
            ActionButtonDefinition<?, ?> actionButton = createButtonDefinition(label, actionButtonInterface);
            AddActionButtonEvent.fire(this, historyToken, actionButton, actionButtonInterface.isInMoreMenu());
        }
    }

    /**
     * Adds new action button to standard table-based sub tab.
     */
    public void addDetailPlaceActionButton(EntityType mainTabEntityType, String detailPlaceId,
            String label, ActionButtonInterface actionButtonInterface) {
        String historyToken = mainTabEntityType.getSubTabHistoryToken(detailPlaceId);

        if (historyToken != null) {
            ActionButtonDefinition<?, ?> actionButton = createButtonDefinition(label, actionButtonInterface);
            AddActionButtonEvent.fire(this, historyToken, actionButton, actionButtonInterface.isInMoreMenu());
        }
    }

    <E, T> ActionButtonDefinition<E, T> createButtonDefinition(String label, ActionButtonInterface actionButtonInterface) {
        return new AbstractButtonDefinition<E, T>(eventBus, label) {

            @Override
            public void onClick(E mainEntity, List<T> selectedItems) {
                JsArray<?> invokeArgs = JsArrayHelper.createMixedArray(
                        EntityObject.arrayFrom(selectedItems),
                        EntityObject.from(mainEntity));

                actionButtonInterface.onClick().invoke(invokeArgs, null);
            }

            @Override
            public boolean isEnabled(E mainEntity, List<T> selectedItems) {
                JsArray<?> invokeArgs = JsArrayHelper.createMixedArray(
                        EntityObject.arrayFrom(selectedItems),
                        EntityObject.from(mainEntity));

                return JsFunctionResultHelper.invokeAndGetResultAsBoolean(
                        actionButtonInterface.isEnabled(),
                        invokeArgs, null, true);
            }

            @Override
            public boolean isAccessible(E mainEntity, List<T> selectedItems) {
                JsArray<?> invokeArgs = JsArrayHelper.createMixedArray(
                        EntityObject.arrayFrom(selectedItems),
                        EntityObject.from(mainEntity));

                return JsFunctionResultHelper.invokeAndGetResultAsBoolean(
                        actionButtonInterface.isAccessible(),
                        invokeArgs, null, true);
            }

            @Override
            public int getIndex() {
                return actionButtonInterface.getIndex();
            }

            @Override
            public String getUniqueId() {
                return actionButtonInterface.getId();
            }

        };
    }

    /**
     * Shows a modal dialog with content loaded from the given URL.
     */
    public void showDialog(String title, String dialogToken, String contentUrl,
            String width, String height, DialogOptions options) {
        // Create and initialize the popup
        DynamicUrlContentPopupPresenterWidget popup = dynamicUrlContentPopupPresenterWidgetProvider.get();
        popup.init(dialogToken, title, width, height,
                options.getCloseIconVisible(),
                options.getCloseOnEscKey());
        popup.setContentUrl(contentUrl);

        // Add dialog buttons
        JsArray<DialogButtonInterface> buttons = options.getButtons();
        for (int i = 0; i < buttons.length(); i++) {
            final DialogButtonInterface dialogButtonInterface = buttons.get(i);

            if (dialogButtonInterface != null) {
                popup.addFooterButton(dialogButtonInterface.getLabel(),
                        event -> dialogButtonInterface.onClick().invoke(null, null));
            }
        }

        // Reveal the popup
        RevealRootPopupContentEvent.fire(this, popup);
    }

    /**
     * Sets the content URL for existing modal dialog.
     */
    public void setDialogContentUrl(final String dialogToken, final String contentUrl) {
        Scheduler.get().scheduleDeferred(() -> SetDynamicPopupContentUrlEvent.fire(PluginUiFunctions.this,
                dialogToken, contentUrl));
    }

    /**
     * Closes an existing modal dialog.
     */
    public void closeDialog(final String dialogToken) {
        Scheduler.get().scheduleDeferred(() -> CloseDynamicPopupEvent.fire(PluginUiFunctions.this,
                dialogToken));
    }

    /**
     * Reveals the application place denoted by {@code historyToken}.
     */
    public void revealPlace(final String historyToken) {
        Scheduler.get().scheduleDeferred(() -> placeManager.revealPlace(new PlaceRequest.Builder().nameToken(historyToken).build()));
    }

    /**
     * Applies the given search string, which triggers transition to the corresponding application place.
     */
    public void setSearchString(final String searchString) {
        Scheduler.get().scheduleDeferred(() -> ApplySearchStringEvent.fire(PluginUiFunctions.this, searchString));
    }

    /**
     * Shows an application-wide alert message.
     */
    public void showAlert(final AlertPanel.Type type, final String message, final AlertOptions options) {
        Scheduler.get().scheduleDeferred(() -> alertManager.showAlert(type, SafeHtmlUtils.fromString(message), options.getAutoHideMs().intValue()));
    }

    /**
     * Shows a toast notification.
     */
    public void showToast(String toastType, String message) {
        Scheduler.get().scheduleDeferred(() -> notificationPresenterWidget.createNotification(message, NotificationStatus.from(toastType)));
    }

    /**
     * Returns the current locale string in <a href="http://tools.ietf.org/html/rfc5646">standard format</a>,
     * e.g. {@code en-US}.
     */
    public String getCurrentLocale() {
        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();

        // Replace "default" with "en_US"
        if ("default".equals(currentLocale)) { //$NON-NLS-1$
            currentLocale = "en_US"; //$NON-NLS-1$
        }

        // Replace "_" with "-"
        currentLocale = currentLocale.replace('_', '-');

        return currentLocale;
    }

    /**
     * Returns the current application place.
     */
    public String getCurrentPlace() {
        return placeManager.getCurrentPlaceRequest().getNameToken();
    }

    public TagObject getRootTagNode() {
        return TagObject.from(tagModelProvider.getModel().getRootNode());
    }

}
