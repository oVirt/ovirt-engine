package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.util.ArrayList;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.AbstractCheckboxHeader;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel.MtuSelector;
import org.ovirt.engine.ui.uicommonweb.models.networks.PortSecuritySelectorValue;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.DnsServersWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.ExternalSubnetWidget;
import org.ovirt.engine.ui.webadmin.widget.vnicProfile.VnicProfilesEditor;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public abstract class AbstractNetworkPopupView<T extends NetworkModel> extends AbstractTabbedModelBoundPopupView<T>
    implements AbstractNetworkPopupPresenterWidget.ViewDef<T> {

    private static final int CLUSTERS_TABLE_HEIGHT = 390;
    private static final String HAS_WARNING = "has-warning"; //$NON-NLS-1$

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractNetworkPopupView<?>> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AbstractNetworkPopupView<?>> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @WithElementId
    public SimpleDialogPanel mainPanel;

    @UiField
    @Ignore
    @WithElementId
    public Label mainLabel;

    @UiField
    @Ignore
    @WithElementId
    public Label externalLabel;

    @UiField
    @Ignore
    @WithElementId
    public Label physicalNetworkLabel;

    @UiField
    @Ignore
    @WithElementId
    public Label assignLabel;

    @UiField(provided = true)
    @Path(value = "dataCenters.selectedItem")
    @WithElementId("dataCenter")
    public ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @Path(value = "name.entity")
    @WithElementId
    public StringEntityModelTextBoxOnlyEditor nameEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    public EntityModelWidgetWithInfo nameWidget;

    @UiField
    @Ignore
    @WithElementId
    public Label nameWarningLabel;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    public StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId
    public StringEntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "external.entity")
    @WithElementId("external")
    public EntityModelCheckBoxEditor externalEditor;

    @UiField(provided = true)
    @Path(value = "externalProviders.selectedItem")
    @WithElementId("externalProviders")
    public ListModelListBoxEditor<Provider> externalProviderEditor;

    @UiField(provided = true)
    @Path(value = "usePhysicalNetworkFromDatacenter.entity")
    @WithElementId("physicalNetworkDatacenterRB")
    public EntityModelRadioButtonEditor physicalNetworkDatacenterRadioButtonEditor;

    @UiField(provided = true)
    @Path(value = "usePhysicalNetworkFromCustom.entity")
    @WithElementId("physicalNetworkCustomRB")
    public EntityModelRadioButtonEditor physicalNetworkCustomRadioButtonEditor;

    @UiField(provided = true)
    public InfoIcon physicalNetworkCustomInfo;

    @UiField(provided = true)
    @Path(value = "connectedToPhysicalNetwork.entity")
    @WithElementId("physicalNetworkEditor")
    public EntityModelCheckBoxEditor physicalNetworkEditor;

    @UiField(provided = true)
    @Path(value = "datacenterPhysicalNetwork.selectedItem")
    @WithElementId("datacenterPhysicalNetworkEditor")
    public ListModelListBoxOnlyEditor<Network> datacenterPhysicalNetworkEditor;

    @UiField
    @Path(value = "customPhysicalNetwork.entity")
    @WithElementId("customPhysicalNetwork")
    public StringEntityModelTextBoxOnlyEditor customPhysicalNetworkEditor;

    @UiField(provided = true)
    @Path(value = "isVmNetwork.entity")
    @WithElementId
    public final EntityModelCheckBoxEditor isVmNetworkEditor;

    @UiField(provided = true)
    @Path(value = "portIsolation.entity")
    @WithElementId
    public final EntityModelCheckBoxEditor portIsolationEditor;

    @UiField(provided = true)
    @Path(value = "hasVLanTag.entity")
    @WithElementId
    public final EntityModelCheckBoxEditor vlanTagging;

    @UiField
    @Path(value = "VLanTag.entity")
    @WithElementId
    public IntegerEntityModelTextBoxOnlyEditor vlanTag;

    @UiField
    @Path(value = "mtuSelector.selectedItem")
    @WithElementId
    public ListModelRadioGroupEditor<MtuSelector> mtuSelectorEditor;

    @Path(value = "mtu.entity")
    @WithElementId
    public IntegerEntityModelTextBoxOnlyEditor mtuEditor;

    @UiField
    @Path(value = "portSecuritySelector.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<PortSecuritySelectorValue> portSecuritySelectorEditor;

    @UiField(provided = true)
    @Path(value = "dnsConfigurationModel.shouldSetDnsConfiguration.entity")
    @WithElementId
    public EntityModelCheckBoxEditor shouldSetDnsConfigurationEditor;

    @UiField
    @Ignore
    @WithElementId
    public DnsServersWidget dnsServersWidget;

    @UiField
    @Path(value = "networkLabel.selectedItem")
    @WithElementId
    public ListModelSuggestBoxEditor networkLabel;

    @UiField(provided = true)
    @Path(value = "qos.selectedItem")
    @WithElementId
    public ListModelListBoxEditor<HostNetworkQos> qosEditor;

    @UiField
    @WithElementId
    public UiCommandButton addQosButton;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    public final EntityModelCellTable<ListModel<NetworkClusterModel>> clustersTable;

    @UiField
    @WithElementId
    public Container attachContainer;

    @UiField
    @Ignore
    @WithElementId
    public HTML messageLabel;

    @UiField(provided = true)
    @Path(value = "createSubnet.entity")
    @WithElementId("createSubnet")
    public EntityModelCheckBoxEditor createSubnetEditor;

    @UiField
    @Ignore
    public ExternalSubnetWidget subnetWidget;

    @UiField
    @Ignore
    @WithElementId
    public VnicProfilesEditor profilesEditor;

    @UiField
    @Ignore
    @WithElementId
    protected DialogTab generalTab;

    @UiField
    @Ignore
    @WithElementId
    protected DialogTab clusterTab;

    @UiField
    @Ignore
    @WithElementId
    protected DialogTab profilesTab;

    @UiField
    @Ignore
    @WithElementId
    protected DialogTab subnetTab;

    @UiField
    @WithElementId
    DialogTabPanel tabPanel;

    @UiField
    @Ignore
    @WithElementId
    public Label profilesLabel;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public AbstractNetworkPopupView(EventBus eventBus) {
        super(eventBus);
        // Initialize Editors
        EnableableFormLabel nameLabel = new EnableableFormLabel();
        nameLabel.setText(constants.nameLabel());
        nameEditor = new StringEntityModelTextBoxOnlyEditor();
        nameWidget = new EntityModelWidgetWithInfo(nameLabel, nameEditor);
        dataCenterEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());
        externalProviderEditor = new ListModelListBoxEditor<>(new NameRenderer<Provider>());
        qosEditor = new ListModelListBoxEditor<>(new NameRenderer<HostNetworkQos>());
        externalEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        externalEditor.asCheckBox().addValueChangeHandler(event -> refreshClustersTable());
        datacenterPhysicalNetworkEditor = new ListModelListBoxOnlyEditor<>(new NameRenderer<Network>());
        physicalNetworkDatacenterRadioButtonEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        physicalNetworkCustomRadioButtonEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        physicalNetworkCustomInfo = new InfoIcon(templates.italicText(constants.physicalNetworkCustomInfo()));
        physicalNetworkEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        physicalNetworkEditor.asCheckBox()
                .addValueChangeHandler(event -> {
                    physicalNetworkCustomInfo.setVisible(event.getValue());
                    physicalNetworkLabel.setVisible(event.getValue());
                });
        isVmNetworkEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        portIsolationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vlanTagging = new EntityModelCheckBoxEditor(Align.RIGHT);
        mtuEditor = new IntegerEntityModelTextBoxOnlyEditor();
        mtuEditor.setUsePatternFly(true);
        createSubnetEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        this.clustersTable = new EntityModelCellTable<>(SelectionMode.NONE, true);
        this.clustersTable.setHeight(CLUSTERS_TABLE_HEIGHT + Unit.PX.getType());
        shouldSetDnsConfigurationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        portSecuritySelectorEditor = new ListModelListBoxEditor<>();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initEntityModelCellTable();
        localize();

        nameWarningLabel.setVisible(false);
        dnsServersWidget.setUsePatternFly(true);
    }

    protected void localize() {
        generalTab.setLabel(constants.generalTabNetworkPopup());
        clusterTab.setLabel(constants.clusterTabNetworkPopup());
        profilesTab.setLabel(constants.profilesTabNetworkPopup());
        subnetTab.setLabel(constants.subnetTabNetworkPopup());

        dataCenterEditor.setLabel(constants.networkPopupDataCenterLabel());
        assignLabel.setText(constants.networkPopupAssignLabel());
        nameWidget.setExplanation(templates.italicText(constants.networkNameInfo()));
        descriptionEditor.setLabel(constants.descriptionLabel());
        externalLabel.setText(constants.externalLabel());
        externalEditor.setLabel(constants.externalCheckboxLabel());
        physicalNetworkEditor.setLabel(constants.physicalNetworkCheckboxLabel());

        portSecuritySelectorEditor.setLabel(constants.portSecurityEnabledLabel());
        externalProviderEditor.setLabel(constants.externalProviderLabel());
        networkLabel.setLabel(constants.networkLabel());
        commentEditor.setLabel(constants.commentLabel());
        isVmNetworkEditor.setLabel(constants.vmNetworkLabel());
        portIsolationEditor.setLabel(constants.portIsolationLabel());
        vlanTagging.setLabel(constants.enableVlanTagLabel());
        qosEditor.setLabel(constants.hostNetworkQos());
        createSubnetEditor.setLabel(constants.createSubnetLabel());

        profilesLabel.setText(constants.profilesLabel());
        shouldSetDnsConfigurationEditor.setLabel(constants.shouldSetDnsConfigurationLabel());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void setMessageLabel(String label) {
        messageLabel.setHTML(label);
    }

    Iterable<NetworkClusterModel> getClustersTableItems() {
        ListModel<NetworkClusterModel> tableModel = clustersTable.asEditor().flush();
        return tableModel != null && tableModel.getItems() != null ? tableModel.getItems()
                : new ArrayList<NetworkClusterModel>();
    }

    void refreshClustersTable() {
        clustersTable.asEditor().edit(clustersTable.asEditor().flush());
    }

    void initEntityModelCellTable() {
        AbstractCheckboxHeader assignAllHeader = new AbstractCheckboxHeader() {
            @Override
            protected void selectionChanged(Boolean value) {
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    if (networkClusterModel.getIsChangable()) {
                        networkClusterModel.setAttached(value);
                        networkClusterModel.setRequired(value && networkClusterModel.isRequired());
                    }
                }
                refreshClustersTable();
            }

            @Override
            public Boolean getValue() {
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    if (networkClusterModel.getIsChangable() && !networkClusterModel.isAttached()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean isEnabled() {
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    if (networkClusterModel.getIsChangable()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void render(Context context, SafeHtmlBuilder sb) {
                super.render(context, sb);
                sb.append(templates.textForCheckBox(constants.attachAll()));
            }
        };
        AbstractCheckboxHeader requiredAllHeader = new AbstractCheckboxHeader() {
            @Override
            protected void selectionChanged(Boolean value) {
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    networkClusterModel.setRequired(value && networkClusterModel.isAttached());
                }
                refreshClustersTable();
            }

            @Override
            public Boolean getValue() {
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    if (!networkClusterModel.isRequired()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean isEnabled() {
                return isRequiredChangeable();
            }

            @Override
            public void render(Context context, SafeHtmlBuilder sb) {
                super.render(context, sb);
                sb.append(templates.textForCheckBox(constants.requireAll()));
            }
        };

        clustersTable.addColumn(new AbstractTextColumn<NetworkClusterModel>() {
            @Override
            public String getValue(NetworkClusterModel model) {
                return model.getName();
            }
        }, constants.nameClusterHeader());

        clustersTable.addColumn(new AbstractCheckboxColumn<NetworkClusterModel>((index, model, value) -> {
            model.setAttached(value);
            model.setRequired(value && model.isRequired());
            refreshClustersTable();
        }) {
            @Override
            public Boolean getValue(NetworkClusterModel model) {
                return model.isAttached();
            }

            @Override
            protected boolean canEdit(NetworkClusterModel model) {
                return model.getIsChangable();
            }

            @Override
            public void render(Context context, NetworkClusterModel object, SafeHtmlBuilder sb) {
                super.render(context, object, sb);
                sb.append(templates.textForCheckBox(constants.attach()));
            }

        }, assignAllHeader, "150px"); //$NON-NLS-1$
        clustersTable.addColumn(new AbstractCheckboxColumn<NetworkClusterModel>((index, model, value) -> {
            model.setRequired(value && model.isAttached());
            refreshClustersTable();
        }) {
            @Override
            public Boolean getValue(NetworkClusterModel model) {
                return model.isRequired();
            }

            @Override
            protected boolean canEdit(NetworkClusterModel model) {
                return isRequiredChangeable() && model.isAttached();
            }

            @Override
            public void render(Context context, NetworkClusterModel object, SafeHtmlBuilder sb) {
                super.render(context, object, sb);
                sb.append(templates.textForCheckBox(constants.require()));
            }

        }, requiredAllHeader, "150px"); //$NON-NLS-1$
    }

    private boolean isRequiredChangeable() {
        return !externalEditor.asCheckBox().getValue();
    }

    @Override
    public void edit(T model) {
        profilesEditor.edit(model.getProfiles());
        subnetWidget.edit(model.getSubnetModel());
        dnsServersWidget.edit(model.getDnsConfigurationModel().getNameServerModelListModel());
        model.getNameWarningEvent().addListener((ev, sender, args) -> {
            boolean hasWarning = (Boolean) sender;
            if (hasWarning) {
                showNameWarning();
            } else {
                hideNameWarning();
            }
        });
    }

    @Override
    public T flush() {
        profilesEditor.flush();
        subnetWidget.flush();
        dnsServersWidget.flush();
        return null;
    }

    @Override
    public void toggleSubnetVisibility(boolean visible) {
        subnetTab.setVisible(visible);
    }

    @Override
    public void updateVisibility() {
        messageLabel.setVisible(false);
        externalLabel.setVisible(ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly));
        physicalNetworkLabel.setVisible(false);
        physicalNetworkCustomInfo.setVisible(false);
    }

    @Override
    public void toggleProfilesVisibility(boolean visible) {
        profilesTab.setVisible(visible);
    }

    @Override
    public UiCommandButton getQosButton() {
        return addQosButton;
    }

    @Override
    public void addMtuEditor() {
        FlowPanel panel = mtuSelectorEditor.asRadioGroup().getPanel(MtuSelector.customMtu);
        panel.add(mtuEditor);
    }

    @Override
    protected void populateTabMap() {
        getTabNameMapping().put(TabName.GENERAL_TAB, this.generalTab.getTabListItem());
        getTabNameMapping().put(TabName.CLUSTERS_TAB, this.clusterTab.getTabListItem());
        getTabNameMapping().put(TabName.PROFILES_TAB, this.profilesTab.getTabListItem());
        getTabNameMapping().put(TabName.SUBNET_TAB, this.subnetTab.getTabListItem());
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }

    private void showNameWarning() {
        nameWarningLabel.setVisible(true);
        nameEditor.addStyleName(HAS_WARNING);
    }

    private void hideNameWarning() {
        nameWarningLabel.setVisible(false);
        nameEditor.removeStyleName(HAS_WARNING);
    }

}
