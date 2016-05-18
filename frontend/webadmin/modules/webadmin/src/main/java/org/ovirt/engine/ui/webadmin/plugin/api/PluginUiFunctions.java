package org.ovirt.engine.ui.webadmin.plugin.api;

import java.util.List;

import org.ovirt.engine.ui.common.presenter.AddTabActionButtonEvent;
import org.ovirt.engine.ui.common.presenter.RedrawDynamicTabContainerEvent;
import org.ovirt.engine.ui.common.presenter.SetDynamicTabAccessibleEvent;
import org.ovirt.engine.ui.common.widget.AlertManager;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.action.AbstractButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.uicommonweb.models.ApplySearchStringEvent;
import org.ovirt.engine.ui.webadmin.place.WebAdminPlaceManager;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityObject;
import org.ovirt.engine.ui.webadmin.plugin.entity.EntityType;
import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunctionResultHelper;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DynamicUrlContentTabProxyFactory;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SetDynamicTabContentUrlEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.CloseDynamicPopupEvent;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DynamicUrlContentPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.SetDynamicPopupContentUrlEvent;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
    private final Provider<DynamicUrlContentPopupPresenterWidget> dynamicUrlContentPopupPresenterWidgetProvider;

    private final WebAdminPlaceManager placeManager;
    private final AlertManager alertManager;

    @Inject
    public PluginUiFunctions(EventBus eventBus,
            DynamicUrlContentTabProxyFactory dynamicUrlContentTabProxyFactory,
            Provider<DynamicUrlContentPopupPresenterWidget> dynamicUrlContentPopupPresenterWidgetProvider,
            WebAdminPlaceManager placeManager,
            AlertManager alertManager) {
        this.eventBus = eventBus;
        this.dynamicUrlContentTabProxyFactory = dynamicUrlContentTabProxyFactory;
        this.dynamicUrlContentPopupPresenterWidgetProvider = dynamicUrlContentPopupPresenterWidgetProvider;
        this.placeManager = placeManager;
        this.alertManager = alertManager;
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    /**
     * Adds new dynamic main tab that shows contents of the given URL.
     */
    public void addMainTab(String label, String historyToken,
            String contentUrl, TabOptions options) {
        addTab(MainTabPanelPresenter.TYPE_RequestTabs,
                MainTabPanelPresenter.TYPE_ChangeTab,
                MainTabPanelPresenter.TYPE_SetTabContent,
                label, historyToken, true, contentUrl, options);
    }

    /**
     * Adds new dynamic sub tab that shows contents of the given URL.
     */
    public void addSubTab(EntityType entityType, String label,
            String historyToken, String contentUrl, TabOptions options) {
        Type<RequestTabsHandler> requestTabsEventType = entityType.getSubTabPanelRequestTabs();
        Type<ChangeTabHandler> changeTabEventType = entityType.getSubTabPanelChangeTab();
        Type<RevealContentHandler<?>> slot = entityType.getSubTabPanelContentSlot();

        if (requestTabsEventType != null && changeTabEventType != null && slot != null) {
            addTab(requestTabsEventType, changeTabEventType, slot,
                    label, historyToken, false, contentUrl, options);
        }
    }

    void addTab(Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType,
            Type<RevealContentHandler<?>> slot,
            String label, String historyToken, boolean isMainTab,
            String contentUrl, TabOptions options) {
        // Create and bind tab presenter proxy
        dynamicUrlContentTabProxyFactory.create(
                requestTabsEventType, changeTabEventType, slot,
                label, options.getPriority().floatValue(),
                historyToken, isMainTab, contentUrl,
                options.getAlignRight() ? Align.RIGHT : Align.LEFT,
                options.getSearchPrefix());

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
                SetDynamicTabContentUrlEvent.fire(PluginUiFunctions.this,
                        historyToken, contentUrl);
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
                SetDynamicTabAccessibleEvent.fire(PluginUiFunctions.this,
                        historyToken, tabAccessible);
            }
        });
    }

    /**
     * Adds new action button to standard table-based main tab.
     */
    public void addMainTabActionButton(EntityType entityType, String label,
            ActionButtonInterface actionButtonInterface) {
        String historyToken = entityType.getMainTabHistoryToken();

        if (historyToken != null) {
            AddTabActionButtonEvent.fire(this, historyToken,
                    createButtonDefinition(label, actionButtonInterface));
        }
    }

    /**
     * Adds new action button to standard table-based sub tab.
     */
    public void addSubTabActionButton(EntityType mainTabEntityType, EntityType subTabEntityType,
            String label, ActionButtonInterface actionButtonInterface) {
        String historyToken = mainTabEntityType.getSubTabHistoryToken(subTabEntityType);

        if (historyToken != null) {
            AddTabActionButtonEvent.fire(this, historyToken,
                    createButtonDefinition(label, actionButtonInterface));
        }
    }

    <T> ActionButtonDefinition<T> createButtonDefinition(String label,
            final ActionButtonInterface actionButtonInterface) {
        return new AbstractButtonDefinition<T>(eventBus,
                label, actionButtonInterface.getLocation()) {

            @Override
            public void onClick(List<T> selectedItems) {
                actionButtonInterface.onClick().invoke(
                        EntityObject.arrayFrom(selectedItems), null);
            }

            @Override
            public boolean isEnabled(List<T> selectedItems) {
                return JsFunctionResultHelper.invokeAndGetResultAsBoolean(
                        actionButtonInterface.isEnabled(),
                        EntityObject.arrayFrom(selectedItems), null, true);
            }

            @Override
            public boolean isAccessible(List<T> selectedItems) {
                return JsFunctionResultHelper.invokeAndGetResultAsBoolean(
                        actionButtonInterface.isAccessible(),
                        EntityObject.arrayFrom(selectedItems), null, true);
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
                options.getResizeEnabled(),
                options.getCloseIconVisible(),
                options.getCloseOnEscKey());
        popup.setContentUrl(contentUrl);

        // Add dialog buttons
        JsArray<DialogButtonInterface> buttons = options.getButtons();
        for (int i = 0; i < buttons.length(); i++) {
            final DialogButtonInterface dialogButtonInterface = buttons.get(i);

            if (dialogButtonInterface != null) {
                popup.addFooterButton(dialogButtonInterface.getLabel(), new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        dialogButtonInterface.onClick().invoke(null, null);
                    }
                });
            }
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
                SetDynamicPopupContentUrlEvent.fire(PluginUiFunctions.this,
                        dialogToken, contentUrl);
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
                CloseDynamicPopupEvent.fire(PluginUiFunctions.this,
                        dialogToken);
            }
        });
    }

    /**
     * Reveals the application place denoted by {@code historyToken}.
     */
    public void revealPlace(final String historyToken) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                placeManager.revealPlace(new PlaceRequest.Builder().nameToken(historyToken).build());
            }
        });
    }

    /**
     * Applies the given search string, which triggers transition to the corresponding application place.
     */
    public void setSearchString(final String searchString) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                ApplySearchStringEvent.fire(PluginUiFunctions.this, searchString);
            }
        });
    }

    /**
     * Shows an application-wide alert message.
     */
    public void showAlert(final AlertPanel.Type type, final String message, final AlertOptions options) {
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                alertManager.showAlert(type, SafeHtmlUtils.fromString(message), options.getAutoHideMs().intValue());
            }
        });
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

}
