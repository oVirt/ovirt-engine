package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.HostSelectionChangeEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabHostGeneralPresenter extends AbstractSubTabPresenter<VDS, HostListModel, HostGeneralModel, SubTabHostGeneralPresenter.ViewDef, SubTabHostGeneralPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.hostGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
        /**
         * Clear all the alerts currently displayed in the alerts panel of the host.
         */
        void clearAlerts();

        /**
         * Displays a new alert in the alerts panel of the host.
         *
         * @param widget
         *            the widget used to display the alert, usually just a text label, but can also be a text label with
         *            a link to an action embedded
         */
        void addAlert(Widget widget);
    }

    // We need this to get the text of the alert messages:
    private final ApplicationMessages messages;

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            DetailModelProvider<HostListModel, HostGeneralModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.hostGeneralSubTabLabel(), 0, modelProvider);
    }

    @Inject
    public SubTabHostGeneralPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, DetailModelProvider<HostListModel, HostGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider,
                HostSubTabPanelPresenter.TYPE_SetTabContent);

        // Inject a reference to the messages:
        messages = ClientGinjectorProvider.getApplicationMessages();
    }

    @Override
    public void initializeHandlers() {
        super.initializeHandlers();

        // Initialize the list of alerts:
        final HostGeneralModel model = getModelProvider().getModel();
        updateAlerts(getView(), model);

        // Listen for changes in the properties of the model in order
        // to update the alerts panel:
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (args.propertyName.contains("Alert")) { //$NON-NLS-1$
                    updateAlerts(getView(), model);
                }
            }
        });
    }

    /**
     * Review the model and if there are alerts add them to the view.
     *
     * @param view
     *            the view where alerts should be added
     * @param model
     *            the model to review
     */
    private void updateAlerts(final ViewDef view, final HostGeneralModel model) {
        // Clear all the alerts:
        view.clearAlerts();

        // Review the alerts and add those that are active:
        if (model.getHasUpgradeAlert()) {
            addTextAlert(view, messages.hostHasUpgradeAlert());
        }
        if (model.getHasReinstallAlertNonResponsive()) {
            addTextAlert(view, messages.hostHasReinstallAlertNonResponsive());
        }
        if (model.getHasNICsAlert()) {
            addTextAndLinkAlert(view, messages.hostHasNICsAlert(), model.getSaveNICsConfigCommand());
        }
        if (model.getHasManualFenceAlert()) {
            addTextAlert(view, messages.hostHasManualFenceAlert());
        }
        if (ApplicationModeHelper.getUiMode() != ApplicationMode.GlusterOnly && model.getHasNoPowerManagementAlert()) {
            addTextAndLinkAlert(view, messages.hostHasNoPowerManagementAlert(), model.getEditHostCommand());
        }
        if (model.getNonOperationalReasonEntity() != null) {
            addTextAlert(view, EnumTranslator.getInstance().get(model.getNonOperationalReasonEntity()));
        }
    }

    /**
     * Create a widget containing text and add it to the alerts panel of the host.
     *
     * @param view
     *            the view where the alert should be added
     * @param text
     *            the text content of the alert
     */
    private void addTextAlert(final ViewDef view, final String text) {
        final Label label = new Label(text);
        view.addAlert(label);
    }

    /**
     * Create a widget containing text and a link that triggers the execution of a command.
     *
     * @param view
     *            the view where the alert should be added
     * @param text
     *            the text content of the alert
     * @param command
     *            the command that should be executed when the link is clicked
     */
    private void addTextAndLinkAlert(final ViewDef view, final String text, final UICommand command) {
        // Find the open and close positions of the link within the message:
        final int openIndex = text.indexOf("<a>"); //$NON-NLS-1$
        final int closeIndex = text.indexOf("</a>"); //$NON-NLS-1$
        if (openIndex == -1 || closeIndex == -1 || closeIndex < openIndex) {
            return;
        }

        // Extract the text before, inside and after the tags:
        final String beforeText = text.substring(0, openIndex);
        final String betweenText = text.substring(openIndex + 3, closeIndex);
        final String afterText = text.substring(closeIndex + 4);

        // Create a flow panel containing the text and the link:
        final FlowPanel alertPanel = new FlowPanel();

        // Create the label for the text before the tag:
        final Label beforeLabel = new Label(beforeText);
        beforeLabel.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        alertPanel.add(beforeLabel);

        // Create the anchor:
        final Anchor betweenAnchor = new Anchor(betweenText);
        betweenAnchor.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        alertPanel.add(betweenAnchor);

        // Add a listener to the anchor so that the command is executed when
        // it is clicked:
        betweenAnchor.addClickHandler(
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        command.execute();
                    }
                }
                );

        // Create the label for the text after the tag:
        final Label afterLabel = new Label(afterText);
        afterLabel.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
        alertPanel.add(afterLabel);

        // Add the alert to the view:
        view.addAlert(alertPanel);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(ApplicationPlaces.hostMainTabPlace);
    }

    @ProxyEvent
    public void onHostSelectionChange(HostSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
