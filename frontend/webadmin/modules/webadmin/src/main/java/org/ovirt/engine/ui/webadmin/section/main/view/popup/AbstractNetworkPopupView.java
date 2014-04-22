package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.table.header.CheckboxHeader;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel.MtuSelector;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.ExternalSubnetWidget;
import org.ovirt.engine.ui.webadmin.widget.vnicProfile.VnicProfilesEditor;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public abstract class AbstractNetworkPopupView<T extends NetworkModel> extends AbstractTabbedModelBoundPopupView<T>
    implements AbstractNetworkPopupPresenterWidget.ViewDef<T> {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractNetworkPopupView<?>> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    public SimpleDialogPanel mainPanel;

    @UiField
    @Ignore
    public Label mainLabel;

    @UiField
    @Ignore
    public Label exportLabel;

    @UiField
    @Ignore
    public Label assignLabel;

    @UiField(provided = true)
    @Path(value = "dataCenters.selectedItem")
    @WithElementId("dataCenter")
    public ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField
    @Path(value = "name.entity")
    public StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    public StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    public StringEntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "export.entity")
    @WithElementId("export")
    public EntityModelCheckBoxEditor exportEditor;

    @UiField(provided = true)
    @Path(value = "externalProviders.selectedItem")
    @WithElementId("externalProviders")
    public ListModelListBoxEditor<Provider> externalProviderEditor;

    @UiField
    @Path(value = "neutronPhysicalNetwork.entity")
    @WithElementId("neutronPhysicalNetwork")
    public StringEntityModelTextBoxEditor neutronPhysicalNetwork;

    @UiField(provided = true)
    @Path(value = "isVmNetwork.entity")
    public final EntityModelCheckBoxEditor isVmNetworkEditor;

    @UiField(provided = true)
    @Path(value = "hasVLanTag.entity")
    public final EntityModelCheckBoxEditor vlanTagging;

    @UiField
    @Path(value = "VLanTag.entity")
    public IntegerEntityModelTextBoxOnlyEditor vlanTag;

    @UiField
    @Path(value = "mtuSelector.selectedItem")
    public ListModelRadioGroupEditor<MtuSelector> mtuSelectorEditor;

    @Path(value = "mtu.entity")
    public IntegerEntityModelTextBoxOnlyEditor mtuEditor;

    @UiField
    @Path(value = "networkLabel.selectedItem")
    public ListModelSuggestBoxEditor networkLabel;

    @UiField(provided = true)
    @Path(value = "qos.selectedItem")
    public ListModelListBoxEditor<NetworkQoS> qosEditor;

    @UiField
    UiCommandButton addQosButton;

    @UiField(provided = true)
    @Ignore
    public final EntityModelCellTable<ListModel<NetworkClusterModel>> clustersTable;

    @UiField
    public VerticalPanel attachPanel;

    @UiField
    @Ignore
    public HTML messageLabel;

    @UiField
    public WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "createSubnet.entity")
    @WithElementId("createSubnet")
    public EntityModelCheckBoxEditor createSubnetEditor;

    @UiField
    @Ignore
    ExternalSubnetWidget subnetWidget;

    @UiField
    @Ignore
    public VnicProfilesEditor profilesEditor;

    @UiField
    @Ignore
    protected DialogTab generalTab;

    @UiField
    @Ignore
    protected DialogTab clusterTab;

    @UiField
    @Ignore
    protected DialogTab profilesTab;

    @UiField
    @Ignore
    protected DialogTab subnetTab;

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    @Ignore
    public Label profilesLabel;

    @Inject
    public AbstractNetworkPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates, final ApplicationMessages messages) {
        super(eventBus, resources);
        // Initialize Editors
        dataCenterEditor = new ListModelListBoxEditor<StoragePool>(new NullSafeRenderer<StoragePool>() {
            @Override
            public String renderNullSafe(StoragePool storagePool) {
                return storagePool.getName();
            }
        });
        externalProviderEditor = new ListModelListBoxEditor<Provider>(new NullSafeRenderer<Provider>() {
            @Override
            public String renderNullSafe(Provider provider) {
                return provider.getName();
            }
        });
        qosEditor = new ListModelListBoxEditor<NetworkQoS>(new NullSafeRenderer<NetworkQoS>() {
            @Override
            protected String renderNullSafe(NetworkQoS qos) {
                return qos.getName();
            }
        });
        exportEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        exportEditor.asCheckBox().addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                refreshClustersTable();
            }
        });
        isVmNetworkEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vlanTagging = new EntityModelCheckBoxEditor(Align.RIGHT);
        mtuEditor = new IntegerEntityModelTextBoxOnlyEditor();
        createSubnetEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        this.clustersTable = new EntityModelCellTable<ListModel<NetworkClusterModel>>(SelectionMode.NONE, true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTable(constants, templates);
        localize(constants);
        addStyles();
    }

    protected void localize(ApplicationConstants constants) {
        generalTab.setLabel(constants.generalTabNetworkPopup());
        clusterTab.setLabel(constants.clusterTabNetworkPopup());
        profilesTab.setLabel(constants.profilesTabNetworkPopup());
        subnetTab.setLabel(constants.subnetTabNetworkPopup());

        dataCenterEditor.setLabel(constants.networkPopupDataCenterLabel());
        assignLabel.setText(constants.networkPopupAssignLabel());
        nameEditor.setLabel(constants.nameLabel());
        descriptionEditor.setLabel(constants.descriptionLabel());
        exportLabel.setText(constants.exportLabel());
        exportEditor.setLabel(constants.exportCheckboxLabel());
        externalProviderEditor.setLabel(constants.externalProviderLabel());
        neutronPhysicalNetwork.setLabel(constants.neutronPhysicalNetwork());
        networkLabel.setLabel(constants.networkLabel());
        commentEditor.setLabel(constants.commentLabel());
        isVmNetworkEditor.setLabel(constants.vmNetworkLabel());
        vlanTagging.setLabel(constants.enableVlanTagLabel());
        mtuSelectorEditor.setLabel(constants.mtuLabel());
        qosEditor.setLabel(constants.hostNetworkQos());
        createSubnetEditor.setLabel(constants.createSubnetLabel());

        profilesLabel.setText(constants.profilesLabel());
    }

   protected void addStyles() {
        vlanTag.addContentWidgetStyleName(style.valueBox());
        mtuSelectorEditor.addLabelStyleName(style.noPadding());
        mtuSelectorEditor.addLabelStyleName(style.mtuLabel());
        mtuSelectorEditor.addContentWidgetStyleName(style.mtuSelector());
        mtuEditor.addContentWidgetStyleName(style.valueBox());
        mtuEditor.addWrapperStyleName(style.inlineBlock());
        networkLabel.addContentWidgetStyleName(style.valueBox());
        qosEditor.addContentWidgetStyleName(style.valueBox());
        isVmNetworkEditor.addContentWidgetStyleName(style.vmNetworkStyle());
        isVmNetworkEditor.asCheckBox().addStyleName(style.vmNetworkStyle());
        vlanTagging.addContentWidgetStyleName(style.noPadding());
        vlanTagging.asCheckBox().addStyleName(style.noPadding());
        networkLabel.addLabelStyleName(style.noPadding());
        networkLabel.addLabelStyleName(style.inlineBlock());
        qosEditor.addLabelStyleName(style.noPadding());
        qosEditor.addLabelStyleName(style.inlineBlock());
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

    void initEntityModelCellTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        CheckboxHeader assignAllHeader = new CheckboxHeader(templates.textForCheckBoxHeader(constants.attachAll())) {
            @Override
            protected void selectionChanged(Boolean value) {
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    if (networkClusterModel.getIsChangable()) {
                        networkClusterModel.setAttached(value);
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
        };
        CheckboxHeader requiredAllHeader = new CheckboxHeader(templates.textForCheckBoxHeader(constants.requiredAll())) {
            @Override
            protected void selectionChanged(Boolean value) {
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    networkClusterModel.setRequired(value);
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
        };

        clustersTable.addColumn(new TextColumnWithTooltip<NetworkClusterModel>() {
            @Override
            public String getValue(NetworkClusterModel model) {
                return model.getName();
            }
        }, constants.nameClusterHeader());

        clustersTable.addColumn(new CheckboxColumn<NetworkClusterModel>(new FieldUpdater<NetworkClusterModel, Boolean>() {
            @Override
            public void update(int index, NetworkClusterModel model, Boolean value) {
                model.setAttached(value);
                refreshClustersTable();
            }
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

        }, assignAllHeader, "80px"); //$NON-NLS-1$
        clustersTable.addColumn(new CheckboxColumn<NetworkClusterModel>(new FieldUpdater<NetworkClusterModel, Boolean>() {
            @Override
            public void update(int index, NetworkClusterModel model, Boolean value) {
                model.setRequired(value);
                refreshClustersTable();
            }
        }) {
            @Override
            public Boolean getValue(NetworkClusterModel model) {
                return model.isRequired();
            }

            @Override
            protected boolean canEdit(NetworkClusterModel model) {
                return isRequiredChangeable();
            }

            @Override
            public void render(Context context, NetworkClusterModel object, SafeHtmlBuilder sb) {
                super.render(context, object, sb);
                sb.append(templates.textForCheckBox(constants.required()));
            }

        }, requiredAllHeader, "80px"); //$NON-NLS-1$
    }

    private boolean isRequiredChangeable() {
        return !exportEditor.asCheckBox().getValue();
    }

    @Override
    public void edit(T model) {
        profilesEditor.edit(model.getProfiles());
        subnetWidget.edit(model.getSubnetModel());
    }

    @Override
    public T flush() {
        profilesEditor.flush();
        subnetWidget.flush();
        return null;
    }

    @Override
    public void toggleSubnetVisibility(boolean visible) {
        subnetTab.setVisible(visible);
    }

    @Override
    public void updateVisibility() {
        messageLabel.setVisible(false);
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
        getTabNameMapping().put(TabName.GENERAL_TAB, this.generalTab);
        getTabNameMapping().put(TabName.CLUSTERS_TAB, this.clusterTab);
        getTabNameMapping().put(TabName.PROFILES_TAB, this.profilesTab);
        getTabNameMapping().put(TabName.SUBNET_TAB, this.subnetTab);
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }

    interface WidgetStyle extends CssResource {
        String valueBox();

        String noPadding();

        String mtuSelector();

        String vmNetworkStyle();

        String inlineBlock();

        String mtuLabel();
    }

}
