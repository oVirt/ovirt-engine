package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.Collection;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractHeaderPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.OptionsProvider;
import org.ovirt.engine.ui.common.widget.tab.AbstractTab;
import org.ovirt.engine.ui.common.widget.tab.GroupedTab;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.uicommonweb.models.events.AlertListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.EventListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.UpdateMainContentLayout.ContentDisplayType;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.OverlayPresenter.OverlayType;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure.ConfigurePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;
import org.ovirt.engine.ui.webadmin.widget.alert.ActionWidget;
import org.ovirt.engine.ui.webadmin.widget.tab.WebadminMenuLayout;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class HeaderPresenterWidget extends AbstractHeaderPresenterWidget<HeaderPresenterWidget.ViewDef>
    implements TabWidgetHandler {

    public interface ViewDef extends AbstractHeaderPresenterWidget.ViewDef {

        HasClickHandlers getConfigureLink();

        HasClickHandlers getAboutLink();

        void addTabGroup(String title, int index, HasCssName icon);

        void addTab(String title, int index, String id, String href, String groupTitle, int groupIndex, HasCssName icon);

        void updateTab(String title, String href, boolean accessible);

        void markActiveTab(String text, String href);

        HasData<AuditLog> getEventDropdown();

        HasData<AuditLog> getAlertDropdown();

        HasClickHandlers getTasksWidget();

        HasClickHandlers getBookmarkLink();

        HasClickHandlers getTagsLink();

        void setRunningTaskCount(int count);

        void setAlertCount(int count);

        ActionWidget getEventActionWidget();

        ActionWidget getAlertActionWidget();

    }

    public static final String CONFIGURE_HREF = "configure"; // $NON-NLS-1$

    private final ApplicationConstants constants = AssetProvider.getConstants();

    private final Provider<AboutPopupPresenterWidget> aboutPopupProvider;
    private final Provider<ConfigurePopupPresenterWidget> configurePopupProvider;
    private final TaskModelProvider taskModelProvider;
    private final AlertModelProvider alertModelProvider;
    private final EventModelProvider eventModelProvider;
    private final WebadminMenuLayout menuLayout;

    @Inject
    public HeaderPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user,
            OptionsProvider optionsProvider,
            Provider<AboutPopupPresenterWidget> aboutPopupProvider,
            Provider<ConfigurePopupPresenterWidget> configurePopupProvider,
            ApplicationDynamicMessages dynamicMessages,
            @Named("notification") EventModelProvider eventModelProvider,
            AlertModelProvider alertModelProvider,
            WebadminMenuLayout menuLayout,
            TaskModelProvider taskModelProvider) {
        super(eventBus, view, user, optionsProvider, dynamicMessages.applicationDocTitle(), dynamicMessages.guideUrl());
        this.aboutPopupProvider = aboutPopupProvider;
        this.configurePopupProvider = configurePopupProvider;
        this.taskModelProvider = taskModelProvider;
        this.alertModelProvider = alertModelProvider;
        this.eventModelProvider = eventModelProvider;
        this.menuLayout = menuLayout;
        eventModelProvider.addDataDisplay(getView().getEventDropdown());
        alertModelProvider.addDataDisplay(getView().getAlertDropdown());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onBind() {
        super.onBind();

        insertConfigureMenu();
        registerHandler(getView().getConfigureLink().addClickHandler(event ->
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, configurePopupProvider.get())));

        registerHandler(getView().getAboutLink().addClickHandler(event ->
                RevealRootPopupContentEvent.fire(HeaderPresenterWidget.this, aboutPopupProvider.get())));

        registerHandler(getView().getTasksWidget().addClickHandler(event ->
                UpdateMainContentLayoutEvent.fire(HeaderPresenterWidget.this, ContentDisplayType.OVERLAY, OverlayType.TASKS)));

        registerHandler(getView().getBookmarkLink().addClickHandler(event ->
                UpdateMainContentLayoutEvent.fire(HeaderPresenterWidget.this, ContentDisplayType.OVERLAY, OverlayType.BOOKMARK)));

        registerHandler(getView().getTagsLink().addClickHandler(event ->
                UpdateMainContentLayoutEvent.fire(HeaderPresenterWidget.this, ContentDisplayType.OVERLAY, OverlayType.TAGS)));

        getView().getAlertActionWidget().addAction(constants.dismissAlert(),
                this.alertModelProvider.getModel().getDismissCommand(), (command, log) -> {
                AlertListModel model = alertModelProvider.getModel();
                if (log != null) {
                    model.setSelectedItem(log);
                    model.executeCommand(command);
                }
        });
        getView().getAlertActionWidget().addAllAction(constants.clearAllDismissedAlerts(),
                this.alertModelProvider.getModel().getClearAllCommand(), (command, log) -> {
                alertModelProvider.getModel().executeCommand(command);
        });

        getView().getEventActionWidget().addAction(constants.dismissEvent(),
                this.eventModelProvider.getModel().getDismissCommand(), (command, log) -> {
                EventListModel<?> model = eventModelProvider.getModel();
                if (log != null) {
                    model.setSelectedItem(log);
                    model.executeCommand(command);
                }
        });
        getView().getEventActionWidget().addAllAction(constants.clearAllDismissedEvents(),
                this.eventModelProvider.getModel().getClearAllCommand(), (command, log) -> {
                eventModelProvider.getModel().executeCommand(command);
        });

        taskModelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                Collection<Job> jobs = taskModelProvider.getModel().getItems();
                long count = jobs.stream().filter(job -> JobExecutionStatus.STARTED.equals(job.getStatus())).count();
                // I know I will never have more than 100 items, so I can cast to int.
                getView().setRunningTaskCount((int)count);
            }

        });

        alertModelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                Collection<AuditLog> alerts = alertModelProvider.getModel().getItems();
                long count = alerts.stream().filter(alert -> AuditLogSeverity.ALERT.equals(alert.getSeverity())).count();
                // I know I will never have more than 100 items, so I can cast to int.
                getView().setAlertCount((int) count);
            }

        });
    }

    private void insertConfigureMenu() {
        Scheduler.get().scheduleDeferred(() -> {
            // TODO: This is a hack for adding the configure menu.
            TabDefinition configureMenu = new GroupedTab(new GroupedTabData(menuLayout.getDetails(CONFIGURE_HREF)), null);
            configureMenu.setTargetHistoryToken(CONFIGURE_HREF);
            configureMenu.setText(menuLayout.getDetails(CONFIGURE_HREF).getSecondaryTitle());
            addTabWidget(configureMenu, 5);
        });
    }

    String getHref(TabDefinition tab) {
        String href = "#"; //$NON-NLS-1$
        if (tab instanceof AbstractTab) {
            href = ((AbstractTab)tab).getTargetHistoryToken();
        }
        return href;
    }

    @Override
    public void addTabWidget(TabDefinition tab, int index) {
        getView().addTab(tab.getText(), index, tab.getId(), getHref(tab), tab.getGroupTitle(), tab.getGroupPriority(), tab.getIcon());
    }

    @Override
    public void updateTab(TabDefinition tab) {
        getView().updateTab(tab.getText(), getHref(tab), tab.isAccessible());
    }

    @Override
    public void setActiveTab(TabDefinition tab) {
        getView().markActiveTab(tab.getText(), getHref(tab));
    }

}
