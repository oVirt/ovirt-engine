package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.table.header.CheckboxHeader;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public abstract class AbstractNetworkPopupView<T extends NetworkModel> extends AbstractModelBoundPopupView<T> implements AbstractNetworkPopupPresenterWidget.ViewDef<T> {

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
    public ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField
    @Path(value = "name.entity")
    public EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    public EntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    public EntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "export.entity")
    @WithElementId("export")
    public EntityModelCheckBoxEditor exportEditor;

    @UiField(provided = true)
    @Path(value = "externalProviders.selectedItem")
    @WithElementId("externalProviders")
    public ListModelListBoxEditor<Object> externalProviderEditor;

    @UiField(provided = true)
    @Path(value = "isVmNetwork.entity")
    public final EntityModelCheckBoxEditor isVmNetworkEditor;

    @UiField(provided = true)
    @Path(value = "hasVLanTag.entity")
    public final EntityModelCheckBoxEditor vlanTagging;

    @UiField
    @Path(value = "VLanTag.entity")
    public EntityModelTextBoxOnlyEditor vlanTag;

    @UiField(provided = true)
    @Path(value = "hasMtu.entity")
    public final EntityModelCheckBoxEditor hasMtuEditor;

    @UiField
    @Path(value = "mtu.entity")
    public EntityModelTextBoxOnlyEditor mtuEditor;

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
    public final EntityModelCellTable<ListModel> clustersTable;

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
    public DialogTab generalTab;

    @UiField
    @Ignore
    public DialogTab clusterTab;

    @UiField
    @Ignore
    public DialogTab profilesTab;

    @UiField
    @Ignore
    public DialogTab subnetTab;

    @UiField
    @Ignore
    public Label profilesLabel;

 @Inject
 public AbstractNetworkPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources);
        // Initialize Editors
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((StoragePool) object).getName();
            }
        });
        externalProviderEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Provider) object).getName();
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
                for (NetworkClusterModel networkClusterModel : getClustersTableItems()) {
                    networkClusterModel.setRequired(!event.getValue());
                    refreshClustersTable();
                }
            }
        });
        isVmNetworkEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vlanTagging = new EntityModelCheckBoxEditor(Align.RIGHT);
        hasMtuEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        createSubnetEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        this.clustersTable = new EntityModelCellTable<ListModel>(SelectionMode.NONE, true);
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
        networkLabel.setLabel(constants.networkLabel());
        commentEditor.setLabel(constants.commentLabel());
        isVmNetworkEditor.setLabel(constants.vmNetworkLabel());
        vlanTagging.setLabel(constants.enableVlanTagLabel());
        hasMtuEditor.setLabel(constants.overrideMtuLabel());
        qosEditor.setLabel(constants.hostNetworkQos());
        createSubnetEditor.setLabel(constants.createSubnetLabel());

        profilesLabel.setText(constants.profilesLabel());
    }

   protected void addStyles() {
        vlanTag.addContentWidgetStyleName(style.valueBox());
        mtuEditor.addContentWidgetStyleName(style.valueBox());
        networkLabel.addContentWidgetStyleName(style.valueBox());
        qosEditor.addContentWidgetStyleName(style.valueBox());
        isVmNetworkEditor.addContentWidgetStyleName(style.vmNetworkStyle());
        isVmNetworkEditor.asCheckBox().addStyleName(style.vmNetworkStyle());
        vlanTagging.addContentWidgetStyleName(style.checkBox());
        vlanTagging.asCheckBox().addStyleName(style.checkBox());
        hasMtuEditor.addContentWidgetStyleName(style.checkBox());
        hasMtuEditor.asCheckBox().addStyleName(style.checkBox());
        networkLabel.addLabelStyleName(style.checkBox());
        networkLabel.addLabelStyleName(style.inlineLabel());
        qosEditor.addLabelStyleName(style.checkBox());
        qosEditor.addLabelStyleName(style.inlineLabel());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void setMessageLabel(String label) {
        messageLabel.setHTML(label);
    }

    @SuppressWarnings("unchecked")
    Iterable<NetworkClusterModel> getClustersTableItems() {
        ListModel tableModel = clustersTable.asEditor().flush();
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

        clustersTable.addEntityModelColumn(new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel model) {
                return ((NetworkClusterModel) model).getName();
            }
        }, constants.nameClusterHeader());

        clustersTable.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                NetworkClusterModel networkClusterModel = (NetworkClusterModel) model;
                networkClusterModel.setAttached(value);
                refreshClustersTable();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((NetworkClusterModel) model).isAttached();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return model.getIsChangable();
            }

            @Override
            public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
                super.render(context, object, sb);
                sb.append(templates.textForCheckBox(constants.attach()));
            }

        }, assignAllHeader, "80px"); //$NON-NLS-1$
        clustersTable.addColumn(new CheckboxColumn<EntityModel>(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                NetworkClusterModel networkClusterModel = (NetworkClusterModel) model;
                networkClusterModel.setRequired(value);
                refreshClustersTable();
            }
        }) {
            @Override
            public Boolean getValue(EntityModel model) {
                return ((NetworkClusterModel) model).isRequired();
            }

            @Override
            protected boolean canEdit(EntityModel model) {
                return isRequiredChangeable();
            }

            @Override
            public void render(Context context, EntityModel object, SafeHtmlBuilder sb) {
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

    interface WidgetStyle extends CssResource {
        String valueBox();

        String checkBox();

        String vmNetworkStyle();

        String inlineLabel();
    }

}
