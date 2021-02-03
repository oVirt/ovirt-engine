package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabClusterGeneralPresenter
    extends AbstractSubTabClusterPresenter<ClusterGeneralModel, SubTabClusterGeneralPresenter.ViewDef,
        SubTabClusterGeneralPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.clusterGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabClusterGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Cluster> {
        /**
         * Clear all the alerts currently displayed in the alerts panel of the cluster.
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

    @TabInfo(container = ClusterSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.CLUSTER_GENERAL;
    }

    @Inject
    public SubTabClusterGeneralPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            ClusterMainSelectedItems selectedItems,
            PlaceManager placeManager, DetailModelProvider<ClusterListModel<Void>, ClusterGeneralModel> modelProvider) {
        // View has no action panel, passing null
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                ClusterSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    public void initializeHandlers() {
        super.initializeHandlers();

        // Initialize the list of alerts:
        final ClusterGeneralModel model = getModelProvider().getModel();
        updateAlerts(getView(), model);

        // Listen for changes in the properties of the model in order
        // to update the alerts panel:
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args.propertyName.contains("Alert")) { //$NON-NLS-1$
                updateAlerts(getView(), model);
            } else if (args.propertyName.contains("consoleAddressPartiallyOverridden")) { //$NON-NLS-1$
                updateAlerts(getView(), model);
            } else if (args.propertyName.equals(ClusterGeneralModel.CPU_VERB_PROPERTY_CHANGE)) { //$NON-NLS-1$
                updateAlerts(getView(), model);
            } else if (args.propertyName.equals(ClusterGeneralModel.CONFIGURED_CPU_VERB_PROPERTY_CHANGE)) {
                updateAlerts(getView(), model);
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
    private void updateAlerts(final ViewDef view, final ClusterGeneralModel model) {
        // Clear all the alerts:
        view.clearAlerts();

        // Review the alerts and add those that are active:
        if (model.getHasNewGlusterHostsAlert()) {
            addTextAndLinkAlert(view,
                    messages.clusterHasNewGlusterHosts(),
                    model.getImportNewGlusterHostsCommand(),
                    model.getDetachNewGlusterHostsCommand());
        }

        if (model.isConsoleAddressPartiallyOverridden()) {
            view.addAlert(new Label(constants.consolePartiallyOverridden()));
        }

        if (model.isCpuConfigurationOutdated()) {
            view.addAlert(new Label(messages.clusterCpuConfigurationOutdated(model.getCpuVerb(), model.getConfiguredCpuVerb())));
        }
    }

    /**
     * Create a widget containing text and a link that triggers the execution of a command.
     *
     * @param view
     *            the view where the alert should be added
     * @param text
     *            the text content of the alert
     * @param commands
     *            the command that should be executed when the link is clicked
     */
    private void addTextAndLinkAlert(final ViewDef view, final String text, final UICommand... commands) {
        // Create a flow panel containing the text and the link:
        final FlowPanel alertPanel = new FlowPanel();
        int start = 0;

        for (final UICommand command : commands) {
            // Find the open and close positions of the link within the message:
            final int openIndex = text.indexOf("<a>", start); //$NON-NLS-1$
            final int closeIndex = text.indexOf("</a>", start); //$NON-NLS-1$
            if (openIndex == -1 || closeIndex == -1 || closeIndex < openIndex) {
                break;
            }

            // Extract the text before, inside and after the tags:
            final String beforeText = text.substring(start, openIndex);
            final String betweenText = text.substring(openIndex + 3, closeIndex);
            start = closeIndex + 4;

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
            betweenAnchor.addClickHandler(event -> command.execute());
        }

        if (start < text.length()) {
            final String afterText = text.substring(start);
            // Create the label for the text after the tag:
            final Label afterLabel = new Label(afterText);
            afterLabel.getElement().getStyle().setProperty("display", "inline"); //$NON-NLS-1$ //$NON-NLS-2$
            alertPanel.add(afterLabel);
        }

        if (start > 0) {
            // Add the alert to the view:
            view.addAlert(alertPanel);
        }
    }

}
