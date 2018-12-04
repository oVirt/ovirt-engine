package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.RadioButtonPanel;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostSetupNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.ExternalNetworkPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.InternalNetworkPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.InternalNetworksPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkGroup;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkLabelPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkLabelPanel.NewNetworkLabelPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkPanelsStyle;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.SimpleNetworkItemsPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.AnimatedVerticalPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class HostSetupNetworksPopupView extends AbstractModelBoundPopupView<HostSetupNetworksModel> implements HostSetupNetworksPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<HostSetupNetworksModel, HostSetupNetworksPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostSetupNetworksPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    RadioButtonPanel networksOrLabels;

    @UiField
    Panel networksPanel;

    @UiField
    Panel labelsPanel;

    @UiField
    InternalNetworksPanel internalNetworkList;

    @UiField
    SimpleNetworkItemsPanel<NetworkPanel> externalNetworkList;

    @UiField
    SimpleNetworkItemsPanel<NetworkLabelPanel> labelsList;

    @UiField(provided = true)
    InfoIcon externalNetworksInfo;

    @UiField
    AnimatedVerticalPanel nicList; /* if this panel is contained within a scroll panel, we need to add extra handling for drag-and-drop auto-scrolling. */

    @UiField
    @Ignore
    Alert statusPanel;

    @UiField
    NetworkPanelsStyle style;

    @UiField(provided = true)
    @Path(value = "checkConnectivity.entity")
    EntityModelCheckBoxEditor checkConnectivity;

    @UiField(provided = true)
    @Path(value = "commitChanges.entity")
    EntityModelCheckBoxEditor commitChanges;

    @UiField(provided = true)
    @Path(value = "showVf.entity")
    EntityModelCheckBoxEditor showVf;

    @UiField(provided = true)
    InfoIcon checkConnInfo;

    @UiField(provided = true)
    InfoIcon commitChangesInfo;

    @UiField
    ScrollPanel nicScrollPanel;

    @UiField(provided = true)
    InfoIcon showVfInfo;

    private final Driver driver = GWT.create(Driver.class);
    private final EventBus eventBus;

    private boolean rendered = false;
    private boolean keepStatusText;
    private List<NetworkGroup> nicGroups;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public HostSetupNetworksPopupView(EventBus eventBus) {
        super(eventBus);
        this.eventBus = eventBus;

        checkConnectivity = new EntityModelCheckBoxEditor(Align.RIGHT);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);
        showVf = new EntityModelCheckBoxEditor(Align.RIGHT);
        externalNetworksInfo = new InfoIcon(templates.italicText(constants.externalNetworksInfo()));
        checkConnInfo = new InfoIcon(templates.italicTwoLines(constants.checkConnectivityInfoPart1(), constants.checkConnectivityInfoPart2()));
        commitChangesInfo = new InfoIcon(templates.italicTwoLines(constants.commitChangesInfoPart1(), constants.commitChangesInfoPart2()));
        showVfInfo = new InfoIcon(templates.italicText(constants.showVfInfo()));

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initStatusPanel();
        initUnassignedItemsPanel();

        setupNicListAutoScrolling();
        driver.initialize(this);
    }

    private void initUnassignedItemsPanel() {
        internalNetworkList.setStyle(style);
        externalNetworkList.setStyle(style);
        labelsList.setStyle(style);

        networksOrLabels.addRadioButton(constants.networksPanel(), true, true, event -> onRadioButtonSelection(true));
        networksOrLabels.addRadioButton(constants.labelsPanel(), false, true, event -> onRadioButtonSelection(false));
    }

    private void onRadioButtonSelection(boolean networksPanelSelected) {
        networksPanel.setVisible(networksPanelSelected);
        labelsPanel.setVisible(!networksPanelSelected);
    }

    // Create an auto-scroll adapter for the nicList's parent ScrollPanel
    private void setupNicListAutoScrolling() {
        if (nicList.getParent() instanceof ScrollPanel) {
            ScrollPanel sp = (ScrollPanel)nicList.getParent();
            new AutoScrollAdapter(eventBus, sp);
        }
    }

    @Override
    public void edit(HostSetupNetworksModel uicommonModel) {
        driver.edit(uicommonModel);
        uicommonModel.getNicsChangedEvent().addListener((ev, sender, args) -> {
            // this is called after both networks and nics were retrieved
            HostSetupNetworksModel model = (HostSetupNetworksModel) sender;
            if (!keepStatusText) {
                initStatusPanel();
            }
            keepStatusText = false;

            int scrollPosition = nicScrollPanel.getVerticalScrollPosition();

            updateNetworks(model.getNetworkModels());
            updateLabels(model.getNewNetworkLabelModel(), model.getLabelModels());
            updateNics(model.getNicModels());

            nicScrollPanel.setVerticalScrollPosition(scrollPosition);
            // mark as rendered
            rendered = true;
            showVf.setVisible(!uicommonModel.isVfsMapEmpty());
            showVfInfo.setVisible(!uicommonModel.isVfsMapEmpty());
        });

        uicommonModel.getOperationCandidateEvent().addListener((ev, sender, args) -> {

            NetworkOperation candidate = args.getCandidate();
            NetworkItemModel<?> op1 = args.getOp1();
            NetworkItemModel<?> op2 = args.getOp2();

            if (candidate == null) {
                setErrorStatus(constants.noValidActionSetupNetwork());
            } else {
                if (candidate.isErroneousOperation()) {
                    setErrorStatus(candidate.getMessage(op1, op2));
                } else {
                    if (candidate.isDisplayNetworkAffected(op1, op2)) {
                        setWarningStatus(messages.moveDisplayNetworkWarning(candidate.getMessage(op1,
                                op2)));
                    } else if (candidate.isRequiredNetworkAffected(op1, op2)) {
                        setWarningStatus(messages.detachRequiredNetworkWarning(candidate.getMessage(op1, op2)));
                    } else {
                        setValidStatus(candidate.getMessage(op1, op2));
                    }
                }
            }
        });

        uicommonModel.getLldpChangedEvent().addListener((ev, sender, args) -> {
            nicGroups.forEach(NetworkGroup::redrawPanelTooltip);
        });

        internalNetworkList.setSetupModel(uicommonModel);
        externalNetworkList.setSetupModel(uicommonModel);
        labelsList.setSetupModel(uicommonModel);
        commitChanges.setEnabled(false);
    }

    @Override
    public HostSetupNetworksModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    private void updateNetworks(List<LogicalNetworkModel> allNetworks) {
        internalNetworkList.clear();
        externalNetworkList.clear();
        Collections.sort(allNetworks);
        List<NetworkPanel> staticNetworkPanels = new ArrayList<>();
        List<NetworkPanel> dynamicNetworkPanels = new ArrayList<>();
        for (LogicalNetworkModel network : allNetworks) {
            if (network.getNetwork().isExternal()) {
                dynamicNetworkPanels.add(new ExternalNetworkPanel(network, style));
            } else if (!network.isAttached()) {
                staticNetworkPanels.add(new InternalNetworkPanel(network, style));
            }
        }
        internalNetworkList.addAll(staticNetworkPanels, !rendered);
        externalNetworkList.addAll(dynamicNetworkPanels, !rendered);
    }

    private void updateLabels(NetworkLabelModel newLabelModel, List<NetworkLabelModel> labels) {
        labelsList.clear();

        List<NetworkLabelPanel> labelPanels = new ArrayList<>();
        labelPanels.add(new NewNetworkLabelPanel(newLabelModel, style));

        Collections.sort(labels);
        for (NetworkLabelModel label : labels) {
            if (!label.isAttached()) {
                labelPanels.add(new NetworkLabelPanel(label, style));
            }
        }
        labelsList.addAll(labelPanels, !rendered);
    }

    private void updateNics(List<NetworkInterfaceModel> nics) {
        nicList.clear();
        Collections.sort(nics);
        nicGroups = new ArrayList<>();
        for (NetworkInterfaceModel nic : nics) {
            nicGroups.add(new NetworkGroup(nic, eventBus, style));
        }
        nicList.addAll(nicGroups, !rendered);
    }

    private void initStatusPanel() {
        setValidStatus(constants.dragToMakeChangesSetupNetwork());
    }

    private void setValidStatus(String message) {
        keepStatusText = false;
        statusPanel.setText(message);
        statusPanel.setType(AlertType.INFO);
    }

    private void setWarningStatus(String message) {
        keepStatusText = true;
        statusPanel.setText(message);
        statusPanel.setType(AlertType.WARNING);
    }

    private void setErrorStatus(String message) {
        keepStatusText = false;
        statusPanel.setText(message);
        statusPanel.setType(AlertType.DANGER);
    }
}
