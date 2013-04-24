package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.CheckboxHeader;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
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

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AbstractNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    public SimpleDialogPanel mainPanel;

    @UiField
    @Ignore
    public Label mainLabel;

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

    @UiField(provided = true)
    @Path(value = "isVmNetwork.entity")
    public final EntityModelCheckBoxEditor isVmNetworkEditor;

    @UiField(provided = true)
    @Path(value = "hasVLanTag.entity")
    public final EntityModelCheckBoxEditor vlanTagging;

    @UiField
    @Path(value = "vLanTag.entity")
    public EntityModelTextBoxOnlyEditor vlanTag;

    @UiField(provided = true)
    @Path(value = "hasMtu.entity")
    public final EntityModelCheckBoxEditor hasMtuEditor;

    @UiField
    @Path(value = "mtu.entity")
    public EntityModelTextBoxOnlyEditor mtuEditor;

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
    @Path(value = "publicUse.entity")
    public final EntityModelCheckBoxEditor publicUseEditor;

    @Inject
    public AbstractNetworkPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources);
        // Initialize Editors
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((StoragePool) object).getname();
            }
        });
        isVmNetworkEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vlanTagging = new EntityModelCheckBoxEditor(Align.RIGHT);
        hasMtuEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        publicUseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        this.clustersTable = new EntityModelCellTable<ListModel>(SelectionMode.NONE, true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTable(constants, templates);
        localize(constants);
        addStyles();
    }

    protected void localize(ApplicationConstants constants) {
        dataCenterEditor.setLabel(constants.networkPopupDataCenterLabel());
        assignLabel.setText(constants.networkPopupAssignLabel());
        nameEditor.setLabel(constants.nameLabel());
        descriptionEditor.setLabel(constants.descriptionLabel());
        isVmNetworkEditor.setLabel(constants.vmNetworkLabel());
        vlanTagging.setLabel(constants.enableVlanTagLabel());
        hasMtuEditor.setLabel(constants.overrideMtuLabel());
        publicUseEditor.setLabel(constants.networkPublicUseLabel());
    }

    protected void addStyles() {
        vlanTag.addContentWidgetStyleName(style.vlanEditor());
        mtuEditor.addContentWidgetStyleName(style.mtuEditor());
        isVmNetworkEditor.addContentWidgetStyleName(style.checkBox());
        isVmNetworkEditor.asCheckBox().addStyleName(style.checkBox());
        vlanTagging.addContentWidgetStyleName(style.checkBox());
        vlanTagging.asCheckBox().addStyleName(style.checkBox());
        hasMtuEditor.addContentWidgetStyleName(style.checkBox());
        hasMtuEditor.asCheckBox().addStyleName(style.checkBox());
        publicUseEditor.addContentWidgetStyleName(style.publicUseEditor());
        publicUseEditor.asCheckBox().addStyleName(style.checkBox());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void setVLanTagEnabled(boolean flag) {
        vlanTag.setEnabled(flag);
    }

    @Override
    public void setMtuEnabled(boolean flag) {
        mtuEditor.setEnabled(flag);
    }

    @Override
    public void setMessageLabel(String label) {
        messageLabel.setHTML(label);
    }

    @SuppressWarnings("unchecked")
    Iterable<EntityModel> getClustersTableItems() {
        ListModel tableModel = clustersTable.flush();
        return tableModel != null && tableModel.getItems() != null ? tableModel.getItems()
                : new ArrayList<EntityModel>();
    }

    void refreshClustersTable() {
        clustersTable.edit(clustersTable.flush());
    }

    void initEntityModelCellTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        CheckboxHeader assignAllHeader = new CheckboxHeader(templates.textForCheckBoxHeader(constants.attachAll())) {
            @Override
            protected void selectionChanged(Boolean value) {
                for (EntityModel model : getClustersTableItems()) {
                    NetworkClusterModel networkClusterModel = (NetworkClusterModel) model;
                    if (networkClusterModel.getIsChangable()) {
                        networkClusterModel.setAttached(value);
                    }
                }
                refreshClustersTable();
            }

            @Override
            public Boolean getValue() {
                for (EntityModel model : getClustersTableItems()) {
                    NetworkClusterModel networkClusterModel = (NetworkClusterModel) model;
                    if (networkClusterModel.getIsChangable()) {
                        if (!networkClusterModel.isAttached()) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean isEnabled() {
                for (EntityModel model : getClustersTableItems()) {
                    NetworkClusterModel networkClusterModel = (NetworkClusterModel) model;
                    if (networkClusterModel.getIsChangable()) {
                        return true;
                    }
                }
                return false;
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
    }

    @Override
    public void updateVisibility() {
        messageLabel.setVisible(false);
        publicUseEditor.setVisible(false);
    }

    interface WidgetStyle extends CssResource {
        String mtuEditor();

        String vlanEditor();

        String checkBox();

        String publicUseEditor();
    }

}
