package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.OperationCandidateEventArgs;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostSetupNetworksPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.ExternalNetworkPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.ExternalNetworksPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.InternalNetworkPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.InternalNetworksPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkGroup;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels.NetworkPanelsStyle;
import org.ovirt.engine.ui.webadmin.widget.editor.AnimatedVerticalPanel;
import org.ovirt.engine.ui.webadmin.widget.footer.StatusPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class HostSetupNetworksPopupView extends AbstractModelBoundPopupView<HostSetupNetworksModel> implements HostSetupNetworksPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostSetupNetworksModel, HostSetupNetworksPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostSetupNetworksPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    InternalNetworksPanel internalNetworkList;

    @UiField
    ExternalNetworksPanel externalNetworkList;

    @UiField(provided = true)
    InfoIcon externalNetworksInfo;

    @UiField
    AnimatedVerticalPanel nicList;

    @UiField
    @Ignore
    StatusPanel statusPanel;

    @UiField
    NetworkPanelsStyle style;

    @UiField(provided = true)
    @Path(value = "checkConnectivity.entity")
    EntityModelCheckBoxEditor checkConnectivity;

    @UiField(provided = true)
    @Path(value = "commitChanges.entity")
    EntityModelCheckBoxEditor commitChanges;

    @UiField(provided = true)
    InfoIcon checkConnInfo;

    @UiField(provided = true)
    InfoIcon commitChangesInfo;

    private final Driver driver = GWT.create(Driver.class);

    private boolean rendered = false;
    private boolean keepStatusText;

    private final ApplicationConstants constants;
    private final ApplicationMessages applicationMessages;

    @Inject
    public HostSetupNetworksPopupView(EventBus eventBus,
                                      ApplicationResources resources,
                                      ApplicationConstants constants,
                                      ApplicationTemplates templates,
                                      ApplicationMessages applicationMessages) {
        super(eventBus, resources);

        this.constants = constants;
        this.applicationMessages = applicationMessages;

        checkConnectivity = new EntityModelCheckBoxEditor(Align.RIGHT);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);
        externalNetworksInfo = new InfoIcon(templates.italicText(constants.externalNetworksInfo()), resources);
        checkConnInfo = new InfoIcon(templates.italicTwoLines(constants.checkConnectivityInfoPart1(), constants.checkConnectivityInfoPart2()), resources);
        commitChangesInfo = new InfoIcon(templates.italicTwoLines(constants.commitChangesInfoPart1(), constants.commitChangesInfoPart2()), resources);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initStatusPanel();
        checkConnectivity.setContentWidgetStyleName(style.checkCon());
        commitChanges.setContentWidgetStyleName(style.commitChanges());
        initUnassignedNetworksPanel();
        localize();
        driver.initialize(this);
    }

    private void initUnassignedNetworksPanel() {
        internalNetworkList.setStyle(style);
        externalNetworkList.setStyle(style);
    }

    private void localize() {
        checkConnectivity.setLabel(constants.checkConHostPopup());
        commitChanges.setLabel(constants.saveNetConfigHostPopup());
    }

    @Override
    public void edit(HostSetupNetworksModel uicommonModel) {
        driver.edit(uicommonModel);
        uicommonModel.getNicsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                // this is called after both networks and nics were retrieved
                HostSetupNetworksModel model = (HostSetupNetworksModel) sender;
                List<LogicalNetworkModel> networks = model.getNetworks();
                List<NetworkInterfaceModel> nics = model.getNics();
                if (!keepStatusText) {
                    initStatusPanel();
                }
                keepStatusText = false;
                updateNetworks(networks);
                updateNics(nics);
                // mark as rendered
                rendered = true;
            }
        });

        uicommonModel.getOperationCandidateEvent().addListener(new IEventListener<OperationCandidateEventArgs>() {
            @Override
            public void eventRaised(Event<? extends OperationCandidateEventArgs> ev,
                    Object sender,
                    OperationCandidateEventArgs args) {

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
                            setWarningStatus(applicationMessages.moveDisplayNetworkWarning(candidate.getMessage(op1,
                                    op2)));
                        } else {
                            setValidStatus(candidate.getMessage(op1, op2));
                        }
                    }
                }
            }
        });

        internalNetworkList.setSetupModel(uicommonModel);
        externalNetworkList.setSetupModel(uicommonModel);
    }

    @Override
    public HostSetupNetworksModel flush() {
        return driver.flush();
    }

    private void updateNetworks(List<LogicalNetworkModel> allNetworks) {
        internalNetworkList.clear();
        externalNetworkList.clear();
        Collections.sort(allNetworks);
        List<NetworkPanel> staticNetworkPanels = new ArrayList<NetworkPanel>();
        List<NetworkPanel> dynamicNetworkPanels = new ArrayList<NetworkPanel>();
        for (LogicalNetworkModel network : allNetworks) {
            if (network.getEntity().isExternal()) {
                dynamicNetworkPanels.add(new ExternalNetworkPanel(network, style));
            } else if (!network.isAttached()) {
                staticNetworkPanels.add(new InternalNetworkPanel(network, style));
            }
        }
        internalNetworkList.addAll(staticNetworkPanels, !rendered);
        externalNetworkList.addAll(dynamicNetworkPanels, !rendered);
    }

    private void updateNics(List<NetworkInterfaceModel> nics) {
        nicList.clear();
        Collections.sort(nics);
        List<NetworkGroup> groups = new ArrayList<NetworkGroup>();
        for (NetworkInterfaceModel nic : nics) {
            groups.add(new NetworkGroup(nic, style));
        }
        nicList.addAll(groups, !rendered);
    }

    private void initStatusPanel() {
        setValidStatus(constants.dragToMakeChangesSetupNetwork());
    }

    private void setValidStatus(String message) {
        keepStatusText = false;
        statusPanel.setTextAndStyle(message, style.statusPanel(), style.statusLabel());
    }

    private void setWarningStatus(String message) {
        keepStatusText = true;
        statusPanel.setTextAndStyle(message, style.warningPanel(), style.warningLabel());
    }

    private void setErrorStatus(String message) {
        keepStatusText = false;
        statusPanel.setTextAndStyle(message, style.errorPanel(), style.errorLabel());
    }
}
