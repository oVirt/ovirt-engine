package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkClusterModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.CheckboxHeader;
import org.ovirt.engine.ui.webadmin.widget.table.column.EntityModelCheckboxColumn;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public abstract class DataCenterNetworkPopupView extends AbstractModelBoundPopupView<DataCenterNetworkModel> implements DataCenterNetworkPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<DataCenterNetworkModel, DataCenterNetworkPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DataCenterNetworkPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Ignore
    Label mainLabel;

    @UiField
    @Ignore
    Label assignLabel;

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "isVmNetwork.entity")
    EntityModelCheckBoxEditor isVmNetworkEditor;

    @UiField(provided = true)
    @Path(value = "hasVLanTag.entity")
    EntityModelCheckBoxEditor vlanTagging;

    @UiField
    @Path(value = "vLanTag.entity")
    EntityModelTextBoxOnlyEditor vlanTag;

    @UiField(provided = true)
    @Path(value = "hasMtu.entity")
    EntityModelCheckBoxEditor hasMtuEditor;

    @UiField
    @Path(value = "mtu.entity")
    EntityModelTextBoxOnlyEditor mtuEditor;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> clustersTable;

    @UiField
    @Ignore
    HTML messageLabel;

    @UiField
    @Ignore
    SimpleDialogButton apply;

    @UiField
    WidgetStyle style;

    @Inject
    public DataCenterNetworkPopupView(EventBus eventBus, ApplicationResources resources,
            ApplicationConstants constants, ApplicationTemplates templates) {
        super(eventBus, resources);
        isVmNetworkEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vlanTagging = new EntityModelCheckBoxEditor(Align.RIGHT);
        hasMtuEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        this.clustersTable = new EntityModelCellTable<ListModel>(SelectionMode.NONE, true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initEntityModelCellTable(constants, templates);
        updateVisibility();
        localize(constants);
        addStyles();
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        assignLabel.setText(constants.dataCenterNetworkPopupAssignLabel());
        nameEditor.setLabel(constants.dataCenterPopupNameLabel());
        descriptionEditor.setLabel(constants.dataCenterPopupDescriptionLabel());
        isVmNetworkEditor.setLabel(constants.dataCenterPopupVmNetworkLabel());
        vlanTagging.setLabel(constants.dataCenterPopupEnableVlanTagLabel());
        hasMtuEditor.setLabel(constants.dataCenterPopupEnableMtuLabel());
    }

    void addStyles() {
        vlanTag.addContentWidgetStyleName(style.vlanEditor());
        mtuEditor.addContentWidgetStyleName(style.mtuEditor());
        isVmNetworkEditor.addContentWidgetStyleName(style.checkBox());
        isVmNetworkEditor.asCheckBox().addStyleName(style.checkBox());
        vlanTagging.addContentWidgetStyleName(style.checkBox());
        vlanTagging.asCheckBox().addStyleName(style.checkBox());
        hasMtuEditor.addContentWidgetStyleName(style.checkBox());
        hasMtuEditor.asCheckBox().addStyleName(style.checkBox());
        apply.setCustomContentStyle(style.applyEnabled());
    }

    @Override
    public void edit(DataCenterNetworkModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public DataCenterNetworkModel flush() {
        return Driver.driver.flush();
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

    @Override
    public void setInputFieldsEnabled(boolean enabled) {
        nameEditor.setEnabled(enabled);
        descriptionEditor.setEnabled(enabled);
        isVmNetworkEditor.setEnabled(enabled);
        vlanTagging.setEnabled(enabled);
        vlanTag.setEnabled(enabled);
        hasMtuEditor.setEnabled(enabled);
        mtuEditor.setEnabled(enabled);
        messageLabel.setVisible(!enabled);
    }

    @Override
    public HasClickHandlers getApply() {
        return apply;
    }

    void initEntityModelCellTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        CheckboxHeader assignAllHeader = new CheckboxHeader(templates.textForCheckBoxHeader(constants.attachAll())) {
            @Override
            protected void selectionChanged(Boolean value) {
                ListModel tableModel = clustersTable.flush();
                for (Object model : tableModel.getItems()) {
                    NetworkClusterModel networkClusterModel = (NetworkClusterModel) model;
                    if (networkClusterModel.getIsChangable()) {
                        networkClusterModel.setAttached(value);
                    }
                }
                clustersTable.edit(tableModel);
            }

            @Override
            public Boolean getValue() {
                for (Object model : clustersTable.flush().getItems()) {
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
                for (Object model : clustersTable.flush().getItems()) {
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

        clustersTable.addColumn(new EntityModelCheckboxColumn(new FieldUpdater<EntityModel, Boolean>() {
            @Override
            public void update(int index, EntityModel model, Boolean value) {
                NetworkClusterModel networkClusterModel = (NetworkClusterModel) model;
                networkClusterModel.setAttached(value);
                clustersTable.edit(clustersTable.flush());
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
    public void setNetworkClusterList(ListModel networkClusterList) {
        clustersTable.edit(networkClusterList);
    }

    public void updateVisibility() {
    }

    @Override
    public void setApplyEnabled(boolean enabled) {
        apply.setEnabled(enabled);
        if (enabled) {
            apply.setCustomContentStyle(style.applyEnabled());
        } else {
            apply.setCustomContentStyle(style.applyDisabled());
        }
    }

    interface WidgetStyle extends CssResource {
        String mtuEditor();

        String vlanEditor();

        String checkBox();

        String applyEnabled();

        String applyDisabled();
    }

}
